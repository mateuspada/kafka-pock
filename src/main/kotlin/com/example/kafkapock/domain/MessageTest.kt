package com.example.kafkapock.domain

import javax.validation.constraints.NotEmpty

data class MessageTest(
    @field:[NotEmpty] val test: String?
)