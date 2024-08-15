package com.example.build

class EntityConfig {
    lateinit var entityList: Array<Entity>
}

class Entity {
    lateinit var recordName: String
    lateinit var fieldMapping: Array<EntityField>
}

class EntityField {
    lateinit var fieldName: String
    lateinit var xpath: String
}
