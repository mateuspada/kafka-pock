package com.example.kafkapock.domain

import javax.validation.constraints.NotEmpty

data class HelloWorld(
    @field:[NotEmpty] val name: String?
)