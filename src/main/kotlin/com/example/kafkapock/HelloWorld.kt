package com.example.kafkapock

import javax.validation.constraints.NotEmpty

data class HelloWorld(
    @field:[NotEmpty] val name: String?
)