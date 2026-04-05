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
 * Seeds the database with MVP demo data.
 * Idempotent: skips seeding if users already exist.
 */
class SeedService {

    fun seedDevData() {
        val userCount = transaction { UsersTable.selectAll().count() }
        if (userCount > 0L) return

        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        val passwordHash = BCrypt.hashpw("password123", BCrypt.gensalt())

        // Absolute path to video files — resolve relative to project root
        val serverDir = java.io.File(System.getProperty("user.dir"))
        val projectRoot = if (serverDir.name == "server") serverDir.parentFile else serverDir
        val videoBasePath = java.io.File(projectRoot, "VideosKotlinApp").absolutePath

        // ── Users ──────────────────────────────────────────────────
        val alexId = "demo-creator-alex"
        val andreiId = "demo-creator-andrei"
        val deliaId = "demo-creator-delia"
        val razvanId = "demo-creator-razvan"
        val learnerId = "demo-learner-001"

        val users = listOf(
            SeedUser(alexId, "alex@cognia.dev", "Alex", "LICENSED_CREATOR"),
            SeedUser(andreiId, "andrei@cognia.dev", "Andrei", "LICENSED_CREATOR"),
            SeedUser(deliaId, "delia@cognia.dev", "Delia", "LICENSED_CREATOR"),
            SeedUser(razvanId, "razvan@cognia.dev", "Razvan", "LICENSED_CREATOR"),
            SeedUser(learnerId, "demo@cognia.dev", "Demo Learner", "LEARNER")
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
                        else -> "Curious mind eager to learn new things"
                    }
                }
            }
        }

        // ── Fetch category IDs ─────────────────────────────────────
        val catByName = transaction {
            CategoriesTable.selectAll().map { it[CategoriesTable.name] to it[CategoriesTable.id] }
        }.toMap()

        val techCatId = catByName["Technology"] ?: return

        // ── Learner preferences ────────────────────────────────────
        transaction {
            for (catName in listOf("Technology", "Computer Science", "Engineering", "Science")) {
                val catId = catByName[catName] ?: continue
                UserCategoriesTable.insert {
                    it[userId] = learnerId
                    it[categoryId] = catId
                }
            }
        }

        // ── Videos ─────────────────────────────────────────────────
        val videoAlex = "demo-video-alex"
        val videoAndrei = "demo-video-andrei"
        val videoDelia = "demo-video-delia"
        val videoRazvan = "demo-video-razvan"

        data class SeedVideo(
            val id: String,
            val creatorId: String,
            val title: String,
            val description: String,
            val normalFile: String,
            val eli5File: String
        )

        val videos = listOf(
            SeedVideo(
                videoAlex, alexId,
                "Understanding APIs",
                "Alex explains what APIs are and how they work in modern software development.",
                "$videoBasePath/Alex/video_normal2.MP4",
                "$videoBasePath/Alex/video_explainlikefive2.MP4"
            ),
            SeedVideo(
                videoAndrei, andreiId,
                "DHCP Explained",
                "Andrei breaks down the Dynamic Host Configuration Protocol and how devices get IP addresses.",
                "$videoBasePath/Andrei/video_normal4.MP4",
                "$videoBasePath/Andrei/video_explainlikefive.MP4"
            ),
            SeedVideo(
                videoDelia, deliaId,
                "Cloud Computing Basics",
                "Delia introduces cloud computing concepts: IaaS, PaaS, SaaS, and real-world use cases.",
                "$videoBasePath/Delia/video_normal1.mp4",
                "$videoBasePath/Delia/video_explainlikefive1.MP4"
            ),
            SeedVideo(
                videoRazvan, razvanId,
                "Kubernetes 101",
                "Razvan walks through Kubernetes fundamentals: pods, services, and container orchestration.",
                "$videoBasePath/Razvan/video_normal3.MP4",
                "$videoBasePath/Razvan/video_explainlikefive3.MP4"
            )
        )

        transaction {
            for (video in videos) {
                VideosTable.insert {
                    it[id] = video.id
                    it[creatorId] = video.creatorId
                    it[title] = video.title
                    it[description] = video.description
                    it[categoryId] = techCatId
                    it[videoUrl] = video.normalFile
                    it[eli5VideoUrl] = video.eli5File
                    it[thumbnailUrl] = null
                    it[rawFilePath] = null
                    it[status] = "PUBLISHED"
                    it[difficulty] = "EASY"
                    it[createdAt] = now
                    it[updatedAt] = now
                    it[publishedAt] = now
                }
            }
        }

        // ── Quizzes (3 questions, 3 options each) ──────────────────

        data class SeedOption(val text: String)
        data class SeedQuestion(
            val questionText: String,
            val options: List<SeedOption>,
            val correctIndex: Int
        )
        data class SeedQuiz(
            val id: String,
            val creatorId: String,
            val videoId: String,
            val title: String,
            val questions: List<SeedQuestion>
        )

        val quizzes = listOf(
            SeedQuiz(
                "demo-quiz-alex", alexId, videoAlex, "APIs Quiz",
                listOf(
                    SeedQuestion("What does API stand for?",
                        listOf(SeedOption("Application Programming Interface"), SeedOption("Advanced Program Integration"), SeedOption("Automated Processing Input")), 0),
                    SeedQuestion("Which HTTP method is used to retrieve data?",
                        listOf(SeedOption("POST"), SeedOption("GET"), SeedOption("DELETE")), 1),
                    SeedQuestion("What format do most modern APIs use to exchange data?",
                        listOf(SeedOption("XML"), SeedOption("CSV"), SeedOption("JSON")), 2)
                )
            ),
            SeedQuiz(
                "demo-quiz-andrei", andreiId, videoAndrei, "DHCP Quiz",
                listOf(
                    SeedQuestion("What does DHCP stand for?",
                        listOf(SeedOption("Dynamic Host Configuration Protocol"), SeedOption("Direct Hardware Connection Process"), SeedOption("Data Handling Control Protocol")), 0),
                    SeedQuestion("What does DHCP automatically assign to devices?",
                        listOf(SeedOption("A display name"), SeedOption("An IP address"), SeedOption("A MAC address")), 1),
                    SeedQuestion("Which port does DHCP server use?",
                        listOf(SeedOption("Port 80"), SeedOption("Port 443"), SeedOption("Port 67")), 2)
                )
            ),
            SeedQuiz(
                "demo-quiz-delia", deliaId, videoDelia, "Cloud Computing Quiz",
                listOf(
                    SeedQuestion("Which of these is NOT a cloud service model?",
                        listOf(SeedOption("IaaS"), SeedOption("DaaS"), SeedOption("SaaS")), 1),
                    SeedQuestion("What is an example of SaaS?",
                        listOf(SeedOption("AWS EC2"), SeedOption("Gmail"), SeedOption("Docker")), 1),
                    SeedQuestion("What is a key benefit of cloud computing?",
                        listOf(SeedOption("No internet needed"), SeedOption("Scalability on demand"), SeedOption("Free unlimited storage")), 1)
                )
            ),
            SeedQuiz(
                "demo-quiz-razvan", razvanId, videoRazvan, "Kubernetes Quiz",
                listOf(
                    SeedQuestion("What is the smallest deployable unit in Kubernetes?",
                        listOf(SeedOption("Container"), SeedOption("Pod"), SeedOption("Node")), 1),
                    SeedQuestion("What tool is used to manage Kubernetes clusters?",
                        listOf(SeedOption("docker-compose"), SeedOption("kubectl"), SeedOption("npm")), 1),
                    SeedQuestion("What does Kubernetes help orchestrate?",
                        listOf(SeedOption("Databases"), SeedOption("Containers"), SeedOption("Virtual machines")), 1)
                )
            )
        )

        transaction {
            for (quiz in quizzes) {
                QuizzesTable.insert {
                    it[id] = quiz.id
                    it[creatorId] = quiz.creatorId
                    it[videoId] = quiz.videoId
                    it[title] = quiz.title
                    it[quizType] = "MULTIPLE_CHOICE"
                    it[categoryId] = techCatId
                    it[status] = "PUBLISHED"
                    it[difficulty] = "EASY"
                    it[createdAt] = now
                    it[updatedAt] = now
                }
                for ((qIndex, question) in quiz.questions.withIndex()) {
                    val questionId = UUID.randomUUID().toString()
                    QuizQuestionsTable.insert {
                        it[id] = questionId
                        it[quizId] = quiz.id
                        it[questionText] = question.questionText
                        it[correctOptionIndex] = question.correctIndex
                        it[orderIndex] = qIndex
                    }
                    for ((oIndex, option) in question.options.withIndex()) {
                        QuizOptionsTable.insert {
                            it[QuizOptionsTable.id] = UUID.randomUUID().toString()
                            it[QuizOptionsTable.questionId] = questionId
                            it[QuizOptionsTable.text] = option.text
                            it[QuizOptionsTable.orderIndex] = oIndex
                        }
                    }
                }
            }
        }

        // ── Content Views (for feed ordering) ──────────────────────
        transaction {
            val viewCounts = mapOf(
                videoAlex to 100,
                videoAndrei to 80,
                videoDelia to 120,
                videoRazvan to 90
            )
            for ((videoId, count) in viewCounts) {
                repeat(count) {
                    ContentViewsTable.insert {
                        it[id] = UUID.randomUUID().toString()
                        it[userId] = learnerId
                        it[contentId] = videoId
                        it[contentType] = "VIDEO"
                        it[viewedAt] = now
                    }
                }
            }
        }

        println("SeedService: MVP demo data seeded (${users.size} users, ${videos.size} videos, ${quizzes.size} quizzes)")
    }

    private data class SeedUser(
        val id: String,
        val email: String,
        val displayName: String,
        val role: String
    )
}
