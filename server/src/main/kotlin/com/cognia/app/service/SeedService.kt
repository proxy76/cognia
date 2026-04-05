package com.cognia.app.service

import com.cognia.app.database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Seeds the database with deterministic test data for development.
 * Idempotent: skips seeding if users already exist.
 */
class SeedService {

    fun seedDevData() {
        // Always ensure the test creator account exists, even if other users are present
        ensureTestCreator()

        // Only seed the rest of the demo data if no users existed before
        val userCount = transaction { UsersTable.selectAll().count() }
        // The test creator counts as 1 user, so check > 1 (more users means seed already ran or user registered)
        if (userCount > 1L) return

        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        val passwordHash = BCrypt.hashpw("password123", BCrypt.gensalt())

        // ── Users ──────────────────────────────────────────────────
        val adminId = "seed-admin-001"
        val creator1Id = "seed-creator-001"
        val creator2Id = "seed-creator-002"
        val creator3Id = "seed-creator-003"
        val learnerId = "seed-learner-001"
        val learner2Id = "seed-learner-002"
        val testCreatorId = "seed-test-creator-001"

        val users = listOf(
            SeedUser(adminId, "admin@cognia.dev", "Admin User", "ADMIN"),
            SeedUser(creator1Id, "sarah@cognia.dev", "Dr. Sarah Chen", "LICENSED_CREATOR"),
            SeedUser(creator2Id, "marcus@cognia.dev", "Marcus Rivera", "LICENSED_CREATOR"),
            SeedUser(creator3Id, "alex@cognia.dev", "Alex Thompson", "REGULAR_CREATOR"),
            SeedUser(learnerId, "demo@cognia.dev", "Demo Learner", "LEARNER"),
            SeedUser(learner2Id, "jamie@cognia.dev", "Jamie Park", "LEARNER"),
            SeedUser(testCreatorId, "testcreator@cognia.dev", "Test Creator", "LICENSED_CREATOR")
        )

        transaction {
            for (user in users) {
                UsersTable.insert {
                    it[id] = user.id
                    it[email] = user.email
                    it[UsersTable.passwordHash] = passwordHash
                    it[displayName] = user.displayName
                    it[role] = user.role
                    it[authProvider] = "EMAIL"
                    it[createdAt] = now
                    it[updatedAt] = now
                }
                UserProfilesTable.insert {
                    it[userId] = user.id
                    it[level] = if (user.role == "LEARNER") 1 else 5
                    it[totalPoints] = if (user.role == "LEARNER") 0 else 500
                    it[selfDescription] = when (user.role) {
                        "LICENSED_CREATOR" -> "Passionate educator creating quality content"
                        "REGULAR_CREATOR" -> "Sharing what I know with the world"
                        "LEARNER" -> "Curious mind eager to learn new things"
                        else -> null
                    }
                }
            }
        }

        // ── Fetch category IDs ─────────────────────────────────────
        val categories = transaction {
            CategoriesTable.selectAll().map { it[CategoriesTable.id] to it[CategoriesTable.name] }
        }.toMap()
        val catByName = categories.entries.associate { (id, name) -> name to id }

        // ── User category preferences ──────────────────────────────
        transaction {
            val learnerPrefs = listOf("Science", "Technology", "Computer Science", "Mathematics")
            for (catName in learnerPrefs) {
                val catId = catByName[catName] ?: continue
                UserCategoriesTable.insert {
                    it[userId] = learnerId
                    it[categoryId] = catId
                }
            }
            val learner2Prefs = listOf("Art", "Music", "History", "Literature")
            for (catName in learner2Prefs) {
                val catId = catByName[catName] ?: continue
                UserCategoriesTable.insert {
                    it[userId] = learner2Id
                    it[categoryId] = catId
                }
            }
        }

        // ── Videos ─────────────────────────────────────────────────
        data class SeedVideo(
            val id: String,
            val creatorId: String,
            val title: String,
            val description: String,
            val categoryName: String,
            val difficulty: String?,
            val status: String = "PUBLISHED"
        )

        val videos = listOf(
            SeedVideo("seed-video-001", creator1Id, "Intro to Quantum Physics", "A beginner-friendly overview of quantum mechanics principles.", "Science", "MEDIUM"),
            SeedVideo("seed-video-002", creator1Id, "The Wave-Particle Duality", "Exploring how light and matter exhibit both wave and particle properties.", "Physics", "HARD"),
            SeedVideo("seed-video-003", creator2Id, "History of Ancient Rome", "From the founding of Rome to the fall of the Republic.", "History", "EASY"),
            SeedVideo("seed-video-004", creator2Id, "The Renaissance Explained", "Art, culture, and ideas that shaped the modern world.", "History", "MEDIUM"),
            SeedVideo("seed-video-005", creator3Id, "Python for Beginners", "Get started with Python programming from zero.", "Computer Science", "EASY"),
            SeedVideo("seed-video-006", creator3Id, "Learn Guitar Basics", "Your first chords and strumming patterns.", "Music", "EASY"),
            SeedVideo("seed-video-007", creator1Id, "Organic Chemistry Foundations", "Understanding carbon bonds and molecular structures.", "Chemistry", "HARD"),
            SeedVideo("seed-video-008", creator2Id, "Abstract Art Explained", "How to appreciate and understand abstract artwork.", "Art", "MEDIUM"),
            SeedVideo("seed-video-009", creator1Id, "Deep Learning Explained", "Neural networks, backpropagation, and modern AI.", "Technology", "HARD"),
            SeedVideo("seed-video-010", creator3Id, "Philosophy of Mind", "Consciousness, qualia, and the mind-body problem.", "Philosophy", "MEDIUM"),
            SeedVideo("seed-video-011", creator2Id, "Microeconomics 101", "Supply, demand, and market equilibrium.", "Economics", "EASY"),
            SeedVideo("seed-video-012", creator1Id, "Advanced Calculus", "Multivariable calculus and real analysis topics.", "Mathematics", "HARD")
        )

        transaction {
            for (video in videos) {
                val catId = catByName[video.categoryName] ?: continue
                VideosTable.insert {
                    it[id] = video.id
                    it[creatorId] = video.creatorId
                    it[title] = video.title
                    it[description] = video.description
                    it[categoryId] = catId
                    it[videoUrl] = null
                    it[thumbnailUrl] = null
                    it[rawFilePath] = null
                    it[status] = video.status
                    it[difficulty] = video.difficulty
                    it[createdAt] = now
                    it[updatedAt] = now
                    it[publishedAt] = if (video.status == "PUBLISHED") now else null
                }
            }
        }

        // ── Content Views (to make feed ordering meaningful) ───────
        transaction {
            val viewCounts = mapOf(
                "seed-video-001" to 150,
                "seed-video-002" to 80,
                "seed-video-003" to 200,
                "seed-video-004" to 120,
                "seed-video-005" to 300,
                "seed-video-006" to 95,
                "seed-video-007" to 60,
                "seed-video-008" to 110,
                "seed-video-009" to 175,
                "seed-video-010" to 45,
                "seed-video-011" to 130,
                "seed-video-012" to 70
            )
            for ((videoId, count) in viewCounts) {
                repeat(count) { i ->
                    ContentViewsTable.insert {
                        it[id] = UUID.randomUUID().toString()
                        it[userId] = if (i % 2 == 0) learnerId else learner2Id
                        it[contentId] = videoId
                        it[contentType] = "VIDEO"
                        it[viewedAt] = now
                    }
                }
            }
        }

        // ── Quizzes ────────────────────────────────────────────────
        data class SeedOption(val text: String)
        data class SeedQuestion(
            val id: String,
            val questionText: String,
            val options: List<SeedOption>,
            val correctIndex: Int
        )
        data class SeedQuiz(
            val id: String,
            val creatorId: String,
            val title: String,
            val quizType: String,
            val categoryName: String,
            val difficulty: String?,
            val videoId: String?,
            val questions: List<SeedQuestion>
        )

        val quizzes = listOf(
            SeedQuiz(
                "seed-quiz-001", creator1Id, "Quantum Physics Quiz", "MULTIPLE_CHOICE", "Science", "MEDIUM", "seed-video-001",
                listOf(
                    SeedQuestion("seed-qq-001", "What is the capital of France?", listOf(SeedOption("London"), SeedOption("Berlin"), SeedOption("Paris"), SeedOption("Madrid")), 2),
                    SeedQuestion("seed-qq-002", "Which planet is known as the Red Planet?", listOf(SeedOption("Venus"), SeedOption("Mars"), SeedOption("Jupiter"), SeedOption("Saturn")), 1),
                    SeedQuestion("seed-qq-003", "What is the largest mammal?", listOf(SeedOption("Elephant"), SeedOption("Blue Whale"), SeedOption("Giraffe"), SeedOption("Hippopotamus")), 1),
                    SeedQuestion("seed-qq-004", "Who painted the Mona Lisa?", listOf(SeedOption("Van Gogh"), SeedOption("Picasso"), SeedOption("Da Vinci"), SeedOption("Monet")), 2),
                    SeedQuestion("seed-qq-005", "What is the chemical symbol for water?", listOf(SeedOption("O2"), SeedOption("CO2"), SeedOption("H2O"), SeedOption("NaCl")), 2)
                )
            ),
            SeedQuiz(
                "seed-quiz-002", creator3Id, "Python Basics Quiz", "MULTIPLE_CHOICE", "Computer Science", "EASY", "seed-video-005",
                listOf(
                    SeedQuestion("seed-qq-006", "What keyword defines a function in Python?", listOf(SeedOption("func"), SeedOption("def"), SeedOption("function"), SeedOption("fn")), 1),
                    SeedQuestion("seed-qq-007", "What is the output of print(type(5))?", listOf(SeedOption("<class 'int'>"), SeedOption("<class 'float'>"), SeedOption("<class 'str'>"), SeedOption("<class 'num'>")), 0),
                    SeedQuestion("seed-qq-008", "Which data structure uses key-value pairs?", listOf(SeedOption("List"), SeedOption("Tuple"), SeedOption("Dictionary"), SeedOption("Set")), 2)
                )
            ),
            SeedQuiz(
                "seed-quiz-003", creator2Id, "Ancient Rome Quiz", "MULTIPLE_CHOICE", "History", "EASY", "seed-video-003",
                listOf(
                    SeedQuestion("seed-qq-009", "Who was the first Roman Emperor?", listOf(SeedOption("Julius Caesar"), SeedOption("Augustus"), SeedOption("Nero"), SeedOption("Caligula")), 1),
                    SeedQuestion("seed-qq-010", "In what year did Rome traditionally fall?", listOf(SeedOption("376 AD"), SeedOption("410 AD"), SeedOption("476 AD"), SeedOption("500 AD")), 2),
                    SeedQuestion("seed-qq-011", "What was the Roman Senate's meeting place?", listOf(SeedOption("Colosseum"), SeedOption("Curia"), SeedOption("Pantheon"), SeedOption("Forum")), 1),
                    SeedQuestion("seed-qq-012", "Which river flows through Rome?", listOf(SeedOption("Po"), SeedOption("Arno"), SeedOption("Tiber"), SeedOption("Rhine")), 2)
                )
            ),
            SeedQuiz(
                "seed-quiz-004", creator1Id, "Deep Learning Quiz", "MULTIPLE_CHOICE", "Technology", "HARD", "seed-video-009",
                listOf(
                    SeedQuestion("seed-qq-013", "What is a common activation function?", listOf(SeedOption("ReLU"), SeedOption("SQL"), SeedOption("HTTP"), SeedOption("TCP")), 0),
                    SeedQuestion("seed-qq-014", "What does CNN stand for in deep learning?", listOf(SeedOption("Computer Neural Network"), SeedOption("Convolutional Neural Network"), SeedOption("Connected Node Network"), SeedOption("Central Neuron Nexus")), 1),
                    SeedQuestion("seed-qq-015", "What is backpropagation used for?", listOf(SeedOption("Data storage"), SeedOption("Weight updates"), SeedOption("Data collection"), SeedOption("Network routing")), 1)
                )
            ),
            SeedQuiz(
                "seed-quiz-005", creator3Id, "Guitar Basics Quiz", "MULTIPLE_CHOICE", "Music", "EASY", "seed-video-006",
                listOf(
                    SeedQuestion("seed-qq-016", "How many strings does a standard guitar have?", listOf(SeedOption("4"), SeedOption("5"), SeedOption("6"), SeedOption("7")), 2),
                    SeedQuestion("seed-qq-017", "What note is the thickest guitar string tuned to?", listOf(SeedOption("A"), SeedOption("D"), SeedOption("E"), SeedOption("G")), 2),
                    SeedQuestion("seed-qq-018", "What is a chord?", listOf(SeedOption("A single note"), SeedOption("Multiple notes played together"), SeedOption("A rhythm pattern"), SeedOption("A type of guitar")), 1)
                )
            )
        )

        transaction {
            for (quiz in quizzes) {
                val catId = catByName[quiz.categoryName] ?: continue
                QuizzesTable.insert {
                    it[id] = quiz.id
                    it[creatorId] = quiz.creatorId
                    it[videoId] = quiz.videoId
                    it[title] = quiz.title
                    it[quizType] = quiz.quizType
                    it[categoryId] = catId
                    it[status] = "PUBLISHED"
                    it[difficulty] = quiz.difficulty
                    it[createdAt] = now
                    it[updatedAt] = now
                }
                for ((qIndex, question) in quiz.questions.withIndex()) {
                    QuizQuestionsTable.insert {
                        it[id] = question.id
                        it[quizId] = quiz.id
                        it[questionText] = question.questionText
                        it[correctOptionIndex] = question.correctIndex
                        it[orderIndex] = qIndex
                    }
                    for ((oIndex, option) in question.options.withIndex()) {
                        QuizOptionsTable.insert {
                            it[id] = UUID.randomUUID().toString()
                            it[questionId] = question.id
                            it[text] = option.text
                            it[orderIndex] = oIndex
                        }
                    }
                }
            }
        }

        // ── Social: Follows ────────────────────────────────────────
        transaction {
            val followPairs = listOf(
                learnerId to creator1Id,
                learnerId to creator2Id,
                learnerId to creator3Id,
                learner2Id to creator1Id,
                learner2Id to creator2Id,
                creator1Id to creator2Id,
                creator2Id to creator1Id
            )
            for ((follower, followed) in followPairs) {
                FollowsTable.insert {
                    it[followerId] = follower
                    it[followedId] = followed
                    it[createdAt] = now
                }
            }
        }

        // ── Social: Friendships ────────────────────────────────────
        transaction {
            FriendshipsTable.insert {
                it[id] = "seed-friendship-001"
                it[requesterId] = learnerId
                it[receiverId] = learner2Id
                it[status] = "ACCEPTED"
                it[createdAt] = now
                it[acceptedAt] = now
            }
            FriendshipsTable.insert {
                it[id] = "seed-friendship-002"
                it[requesterId] = creator1Id
                it[receiverId] = learnerId
                it[status] = "PENDING"
                it[createdAt] = now
                it[acceptedAt] = null
            }
        }

        // ── Notifications ──────────────────────────────────────────
        transaction {
            val notifications = listOf(
                Triple("Welcome to Cognia!", "Start exploring educational content and quizzes.", "SYSTEM"),
                Triple("New follower", "Dr. Sarah Chen started following you.", "FOLLOW"),
                Triple("Quiz available", "A new quiz is available for Intro to Quantum Physics.", "QUIZ")
            )
            for ((index, notif) in notifications.withIndex()) {
                val (title, body, type) = notif
                NotificationsTable.insert {
                    it[id] = "seed-notif-${index + 1}"
                    it[userId] = learnerId
                    it[NotificationsTable.type] = type
                    it[NotificationsTable.title] = title
                    it[NotificationsTable.body] = body
                    it[referenceId] = null
                    it[referenceType] = null
                    it[read] = 0
                    it[createdAt] = now
                }
            }
        }

        // ── Chat conversations ─────────────────────────────────────
        transaction {
            ChatConversationsTable.insert {
                it[id] = "seed-conv-001"
                it[participantA] = learnerId
                it[participantB] = creator1Id
                it[createdAt] = now
                it[lastMessageAt] = now
            }
            ChatMessagesTable.insert {
                it[id] = "seed-msg-001"
                it[conversationId] = "seed-conv-001"
                it[senderId] = creator1Id
                it[messageType] = "TEXT"
                it[textContent] = "Welcome! Let me know if you have questions about the quantum physics video."
                it[createdAt] = now
            }
            ChatMessagesTable.insert {
                it[id] = "seed-msg-002"
                it[conversationId] = "seed-conv-001"
                it[senderId] = learnerId
                it[messageType] = "TEXT"
                it[textContent] = "Thanks! I really enjoyed the intro video."
                it[createdAt] = now
            }
        }

        println("SeedService: Development data seeded successfully (${users.size} users, ${videos.size} videos, ${quizzes.size} quizzes)")
    }

    private data class SeedUser(
        val id: String,
        val email: String,
        val displayName: String,
        val role: String
    )

    /**
     * Always creates the test creator user if it doesn't already exist.
     * This ensures a LICENSED_CREATOR account is available for upload testing,
     * even when the main seed was skipped because other users were registered.
     */
    private fun ensureTestCreator() {
        val testEmail = "testcreator@cognia.dev"
        val exists = transaction {
            UsersTable.selectAll().where { UsersTable.email eq testEmail }.count() > 0
        }
        if (exists) return

        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        val passwordHash = BCrypt.hashpw("password123", BCrypt.gensalt())
        val testCreatorId = "seed-test-creator-001"

        transaction {
            UsersTable.insert {
                it[id] = testCreatorId
                it[email] = testEmail
                it[UsersTable.passwordHash] = passwordHash
                it[displayName] = "Test Creator"
                it[role] = "LICENSED_CREATOR"
                it[authProvider] = "EMAIL"
                it[createdAt] = now
                it[updatedAt] = now
            }
            UserProfilesTable.insert {
                it[userId] = testCreatorId
                it[level] = 5
                it[totalPoints] = 500
                it[selfDescription] = "Licensed test creator for video upload testing"
            }
        }
        println("SeedService: Created test creator account ($testEmail) with LICENSED_CREATOR role")
    }
}
