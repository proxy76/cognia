package com.cognia.app.service

import com.cognia.app.dto.quiz.*
import com.cognia.app.repository.QuizRepository

class QuizService(
    private val quizRepository: QuizRepository
) {

    fun createQuiz(creatorId: String, request: CreateQuizRequest): QuizDetailResponse {
        val quiz = quizRepository.createQuiz(
            creatorId = creatorId,
            title = request.title,
            quizType = request.quizType,
            categoryId = request.categoryId,
            difficulty = request.difficulty,
            videoId = request.videoId
        )

        val questions = request.questions.mapIndexed { qIndex, qReq ->
            val questionRow = quizRepository.addQuestion(
                quizId = quiz.id,
                questionText = qReq.questionText,
                correctOptionIndex = qReq.correctOptionIndex,
                orderIndex = qIndex
            )

            val options = qReq.options.mapIndexed { oIndex, oReq ->
                quizRepository.addOption(
                    questionId = questionRow.id,
                    text = oReq.text,
                    orderIndex = oIndex
                )
            }

            QuestionResponse(
                id = questionRow.id,
                questionText = questionRow.questionText,
                options = options.map { OptionResponse(it.id, it.text) },
                correctOptionIndex = questionRow.correctOptionIndex
            )
        }

        return QuizDetailResponse(
            id = quiz.id,
            creatorId = quiz.creatorId,
            title = quiz.title,
            quizType = quiz.quizType,
            categoryId = quiz.categoryId,
            difficulty = quiz.difficulty,
            videoId = quiz.videoId,
            status = quiz.status,
            questions = questions
        )
    }

    fun getQuiz(id: String, requesterId: String?): QuizDetailResponse {
        val quiz = quizRepository.findById(id)
            ?: throw QuizNotFoundException(id)

        val questionsWithOptions = quizRepository.getQuestionsWithOptions(id)
        val isOwner = requesterId != null && requesterId == quiz.creatorId

        val questions = questionsWithOptions.map { qwo ->
            QuestionResponse(
                id = qwo.question.id,
                questionText = qwo.question.questionText,
                options = qwo.options.map { OptionResponse(it.id, it.text) },
                correctOptionIndex = if (isOwner) qwo.question.correctOptionIndex else null
            )
        }

        return QuizDetailResponse(
            id = quiz.id,
            creatorId = quiz.creatorId,
            title = quiz.title,
            quizType = quiz.quizType,
            categoryId = quiz.categoryId,
            difficulty = quiz.difficulty,
            videoId = quiz.videoId,
            status = quiz.status,
            questions = questions
        )
    }

    fun submitForReview(id: String, userId: String): QuizDetailResponse {
        val quiz = quizRepository.findById(id)
            ?: throw QuizNotFoundException(id)

        if (quiz.creatorId != userId) {
            throw QuizAccessDeniedException("You do not own this quiz")
        }

        ContentStateMachine.validateTransition(quiz.status, "PENDING_REVIEW")
        quizRepository.updateStatus(id, "PENDING_REVIEW")

        return getQuiz(id, userId)
    }

    fun publishDirect(id: String, userId: String): QuizDetailResponse {
        val quiz = quizRepository.findById(id)
            ?: throw QuizNotFoundException(id)

        if (quiz.creatorId != userId) {
            throw QuizAccessDeniedException("You do not own this quiz")
        }

        ContentStateMachine.validateTransition(quiz.status, "PUBLISHED")
        quizRepository.updateStatus(id, "PUBLISHED")

        return getQuiz(id, userId)
    }

    fun getCreatorQuizzes(creatorId: String): List<QuizSummaryResponse> {
        val quizzes = quizRepository.findByCreatorId(creatorId)
        return quizzes.map { quiz ->
            val questionCount = quizRepository.getQuestionCount(quiz.id)
            QuizSummaryResponse(
                id = quiz.id,
                title = quiz.title,
                quizType = quiz.quizType,
                status = quiz.status,
                questionCount = questionCount
            )
        }
    }
}

class QuizNotFoundException(id: String) : RuntimeException("Quiz not found: $id")
class QuizAccessDeniedException(message: String) : RuntimeException(message)
