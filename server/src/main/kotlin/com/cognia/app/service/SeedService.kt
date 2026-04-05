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
<<<<<<< Updated upstream
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
=======
        val adminId = "seed-admin-001"
        val creator1Id = "seed-creator-001"
        val creator2Id = "seed-creator-002"
        val creator3Id = "seed-creator-003"
        val learnerId = "seed-learner-001"
        val learner2Id = "seed-learner-002"
        val testCreatorId = "seed-test-creator-001"
        // New topic creators
        val andreiId = "seed-creator-andrei"
        val deliaId = "seed-creator-delia"
        val razvanId = "seed-creator-razvan"
        val alexCId = "seed-creator-alex"

        val users = listOf(
            SeedUser(adminId, "admin@cognia.dev", "Admin User", "ADMIN"),
            SeedUser(creator1Id, "sarah@cognia.dev", "Dr. Sarah Chen", "LICENSED_CREATOR"),
            SeedUser(creator2Id, "marcus@cognia.dev", "Marcus Rivera", "LICENSED_CREATOR"),
            SeedUser(creator3Id, "alex@cognia.dev", "Alex Thompson", "REGULAR_CREATOR"),
            SeedUser(learnerId, "demo@cognia.dev", "Demo Learner", "LEARNER"),
            SeedUser(learner2Id, "jamie@cognia.dev", "Jamie Park", "LEARNER"),
            SeedUser(testCreatorId, "testcreator@cognia.dev", "Test Creator", "LICENSED_CREATOR"),
            SeedUser(andreiId, "andrei@cognia.dev", "Andrei", "LICENSED_CREATOR"),
            SeedUser(deliaId, "delia@cognia.dev", "Delia", "LICENSED_CREATOR"),
            SeedUser(razvanId, "razvan@cognia.dev", "Razvan", "LICENSED_CREATOR"),
            SeedUser(alexCId, "alexc@cognia.dev", "Alex", "LICENSED_CREATOR"),
>>>>>>> Stashed changes
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

        // ── Ensure extra categories exist ───────────────��─────────────
        transaction {
            val existing = CategoriesTable.selectAll().map { it[CategoriesTable.name] }.toSet()
            val extras = listOf("Networking" to "networking")
            for ((name, slug) in extras) {
                if (name !in existing) {
                    CategoriesTable.insert {
                        it[id] = UUID.randomUUID().toString()
                        it[CategoriesTable.name] = name
                        it[CategoriesTable.slug] = slug
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
<<<<<<< Updated upstream
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
=======
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
            SeedVideo("seed-video-012", creator1Id, "Advanced Calculus", "Multivariable calculus and real analysis topics.", "Mathematics", "HARD"),
            // ── New topic creator videos ──
            // Andrei — DHCP
            SeedVideo("seed-video-andrei-normal", andreiId, "DHCP Explained", "How Dynamic Host Configuration Protocol assigns IP addresses on a network.", "Networking", "MEDIUM"),
            SeedVideo("seed-video-andrei-eli5", andreiId, "DHCP — Explain Like I'm Five", "Imagine a hotel receptionist giving every guest a room number. That's DHCP!", "Networking", "EASY"),
            // Delia — Cloud Computing
            SeedVideo("seed-video-delia-normal", deliaId, "Cloud Computing Fundamentals", "Understanding cloud infrastructure, SaaS, PaaS, and IaaS models.", "Technology", "MEDIUM"),
            SeedVideo("seed-video-delia-eli5", deliaId, "Cloud Computing — Explain Like I'm Five", "Instead of keeping toys at home, you borrow them from a huge toy library anytime you want.", "Technology", "EASY"),
            // Razvan — Kubernetes
            SeedVideo("seed-video-razvan-normal", razvanId, "Kubernetes Deep Dive", "Container orchestration, pods, services, and deployments in Kubernetes.", "Technology", "HARD"),
            SeedVideo("seed-video-razvan-eli5", razvanId, "Kubernetes — Explain Like I'm Five", "A robot manager that makes sure all your little helper robots are doing their jobs.", "Technology", "EASY"),
            // Alex — APIs
            SeedVideo("seed-video-alex-normal", alexCId, "Understanding APIs", "REST, endpoints, requests and responses — how software talks to software.", "Technology", "MEDIUM"),
            SeedVideo("seed-video-alex-eli5", alexCId, "APIs — Explain Like I'm Five", "Like a waiter taking your order to the kitchen and bringing your food back.", "Technology", "EASY"),
        )

        // Map from normal video ID → ELI5 video ID for the new creators
        val eli5Map = mapOf(
            "seed-video-andrei-normal" to "seed-video-andrei-eli5",
            "seed-video-delia-normal" to "seed-video-delia-eli5",
            "seed-video-razvan-normal" to "seed-video-razvan-eli5",
            "seed-video-alex-normal" to "seed-video-alex-eli5",
        )
        // Map video IDs to their raw file paths in VideosKotlinApp
        val rawFilePaths = mapOf(
            "seed-video-andrei-normal" to "VideosKotlinApp/Andrei/video_normal4.MP4",
            "seed-video-andrei-eli5" to "VideosKotlinApp/Andrei/video_explainlikefive.MP4",
            "seed-video-delia-normal" to "VideosKotlinApp/Delia/video_normal1.mp4",
            "seed-video-delia-eli5" to "VideosKotlinApp/Delia/video_explainlikefive1.MP4",
            "seed-video-razvan-normal" to "VideosKotlinApp/Razvan/video_normal3.MP4",
            "seed-video-razvan-eli5" to "VideosKotlinApp/Razvan/video_explainlikefive3.MP4",
            "seed-video-alex-normal" to "VideosKotlinApp/Alex/video_normal2.MP4",
            "seed-video-alex-eli5" to "VideosKotlinApp/Alex/video_explainlikefive2.MP4",
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
                    it[rawFilePath] = null
                    it[status] = "PUBLISHED"
                    it[difficulty] = "EASY"
=======
                    it[rawFilePath] = rawFilePaths[video.id]
                    it[status] = video.status
                    it[difficulty] = video.difficulty
                    it[eli5VideoId] = eli5Map[video.id]
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
                videoAlex to 100,
                videoAndrei to 80,
                videoDelia to 120,
                videoRazvan to 90
=======
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
                "seed-video-012" to 70,
                // New creator videos
                "seed-video-andrei-normal" to 250,
                "seed-video-delia-normal" to 220,
                "seed-video-razvan-normal" to 190,
                "seed-video-alex-normal" to 210,
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
        println("SeedService: MVP demo data seeded (${users.size} users, ${videos.size} videos, ${quizzes.size} quizzes)")
=======
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
            // ── New topic creator quizzes ──
            SeedQuiz(
                "seed-quiz-andrei", andreiId, "DHCP Quiz", "MULTIPLE_CHOICE", "Networking", "MEDIUM", "seed-video-andrei-normal",
                listOf(
                    SeedQuestion("seed-qq-andrei-1", "What does DHCP stand for?",
                        listOf(SeedOption("Dynamic Host Configuration Protocol"), SeedOption("Direct Host Connection Protocol"), SeedOption("Distributed Hyper Control Protocol")), 0),
                    SeedQuestion("seed-qq-andrei-2", "What does a DHCP server assign to devices on a network?",
                        listOf(SeedOption("MAC addresses"), SeedOption("IP addresses"), SeedOption("Domain names")), 1),
                    SeedQuestion("seed-qq-andrei-3", "Which message does a client send first when requesting an IP via DHCP?",
                        listOf(SeedOption("DHCP Request"), SeedOption("DHCP Acknowledge"), SeedOption("DHCP Discover")), 2),
                )
            ),
            SeedQuiz(
                "seed-quiz-delia", deliaId, "Cloud Computing Quiz", "MULTIPLE_CHOICE", "Technology", "MEDIUM", "seed-video-delia-normal",
                listOf(
                    SeedQuestion("seed-qq-delia-1", "Which of these is a cloud service model?",
                        listOf(SeedOption("SaaS"), SeedOption("BIOS"), SeedOption("HDMI")), 0),
                    SeedQuestion("seed-qq-delia-2", "What is a key benefit of cloud computing?",
                        listOf(SeedOption("Requires no internet"), SeedOption("Scalability on demand"), SeedOption("Only works offline")), 1),
                    SeedQuestion("seed-qq-delia-3", "Which company provides the AWS cloud platform?",
                        listOf(SeedOption("Google"), SeedOption("Microsoft"), SeedOption("Amazon")), 2),
                )
            ),
            SeedQuiz(
                "seed-quiz-razvan", razvanId, "Kubernetes Quiz", "MULTIPLE_CHOICE", "Technology", "HARD", "seed-video-razvan-normal",
                listOf(
                    SeedQuestion("seed-qq-razvan-1", "What is the smallest deployable unit in Kubernetes?",
                        listOf(SeedOption("A Pod"), SeedOption("A Node"), SeedOption("A Cluster")), 0),
                    SeedQuestion("seed-qq-razvan-2", "What tool is commonly used to manage Kubernetes clusters from the command line?",
                        listOf(SeedOption("docker-compose"), SeedOption("kubectl"), SeedOption("npm")), 1),
                    SeedQuestion("seed-qq-razvan-3", "What does Kubernetes primarily orchestrate?",
                        listOf(SeedOption("Virtual machines"), SeedOption("Database schemas"), SeedOption("Containers")), 2),
                )
            ),
            SeedQuiz(
                "seed-quiz-alex", alexCId, "APIs Quiz", "MULTIPLE_CHOICE", "Technology", "MEDIUM", "seed-video-alex-normal",
                listOf(
                    SeedQuestion("seed-qq-alex-1", "What does API stand for?",
                        listOf(SeedOption("Application Programming Interface"), SeedOption("Automated Process Integration"), SeedOption("Advanced Protocol Instruction")), 0),
                    SeedQuestion("seed-qq-alex-2", "Which HTTP method is typically used to retrieve data from a REST API?",
                        listOf(SeedOption("POST"), SeedOption("GET"), SeedOption("DELETE")), 1),
                    SeedQuestion("seed-qq-alex-3", "What data format do most modern REST APIs use?",
                        listOf(SeedOption("CSV"), SeedOption("XML"), SeedOption("JSON")), 2),
                )
            ),
            // ── Original quizzes ──
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

        println("SeedService: Dev data seeded (${users.size} users, ${videos.size} videos, ${quizzes.size} quizzes)")
>>>>>>> Stashed changes
    }

    private data class SeedUser(
        val id: String,
        val email: String,
        val displayName: String,
        val role: String
    )
}
