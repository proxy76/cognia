package com.cognia.app.routes

import com.cognia.app.auth.authorize
import com.cognia.app.dto.category.CreateCategoryRequest
import com.cognia.app.dto.category.UpdateCategoryRequest
import com.cognia.app.service.CategoryAlreadyExistsException
import com.cognia.app.service.CategoryNotFoundException
import com.cognia.app.service.CategoryService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.categoryRoutes() {
    val categoryService by application.inject<CategoryService>()

    route("/api/v1/categories") {
        get {
            val categories = categoryService.findAll()
            call.respond(HttpStatusCode.OK, categories)
        }

        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing id"))
            val category = categoryService.findById(id)
            if (category != null) {
                call.respond(HttpStatusCode.OK, category)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorBody("Category not found"))
            }
        }

        authenticate("auth-jwt") {
            authorize("ADMIN") {
                post {
                    try {
                        val request = call.receive<CreateCategoryRequest>()
                        val category = categoryService.create(request)
                        call.respond(HttpStatusCode.Created, category)
                    } catch (e: CategoryAlreadyExistsException) {
                        call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Category already exists"))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                    }
                }

                put("/{id}") {
                    val id = call.parameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing id"))
                    try {
                        val request = call.receive<UpdateCategoryRequest>()
                        val category = categoryService.update(id, request)
                        call.respond(HttpStatusCode.OK, category)
                    } catch (e: CategoryAlreadyExistsException) {
                        call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Category already exists"))
                    } catch (e: CategoryNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Category not found"))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing id"))
                    try {
                        categoryService.delete(id)
                        call.respond(HttpStatusCode.NoContent)
                    } catch (e: CategoryNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Category not found"))
                    }
                }
            }
        }
    }
}
