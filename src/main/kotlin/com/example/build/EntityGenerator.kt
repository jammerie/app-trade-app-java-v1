package com.example.build

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.io.File
import java.nio.charset.Charset

fun toTitleCase(input: String)  = "${input.substring(0, 1).uppercase()}${input.substring(1)}"
fun toNormalCase(input: String)  = "${input.substring(0, 1).lowercase()}${input.substring(1)}"

fun generateEntity(entity: Entity): String {
    val fieldBody = StringBuilder()
    entity.fieldMapping.forEach { eField->
        val titleCase = toTitleCase(eField.fieldName)
        val fieldName = eField.fieldName
        fieldBody.append("""
    @Column(name = "${fieldName}")
    private String ${fieldName};
    public String get${titleCase} () {
        return ${fieldName};
    }

    public void set${titleCase}(String ${fieldName}) {
        this.${fieldName} = ${fieldName};
    }
        """)

    }

    return """
package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "${entity.recordName}")
public class ${entity.recordName} {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;
    
    // Getters and Setters
    $fieldBody
}
""".trimIndent()
}

fun generateRepository(entity: Entity): String {
    return """
package com.example.repository;

import com.example.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ${entity.recordName}Repository extends JpaRepository<${entity.recordName}, Long> {
}

    """.trimIndent()
}

fun generateService(entity: Entity): String {
    val fieldBody = StringBuilder()
    entity.fieldMapping.forEach { eField ->
        fieldBody.append("""
        record.set${eField.fieldName}(
                getNodeValue(doc, "${eField.xpath}")        
        );
        """)
    }

    return """
package com.example.service;

import com.example.entity.${entity.recordName};
import com.example.repository.${entity.recordName}Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;

@Service
public class ${entity.recordName}Service extends EventProcessorAbstract {
    @Autowired
    private ${entity.recordName}Repository repository;

    @Override
    void processDoc(Document doc) throws XPathExpressionException {
        ${entity.recordName} record = new ${entity.recordName}();

        $fieldBody
        
        repository.save(record);
    }
}
    """.trimIndent()
}

fun main() {
    // read the entity.yaml
    val yamlMapper = YAMLMapper()
    val entityConfigBody = ClassLoader.getSystemClassLoader().getResourceAsStream("entity.yaml").readAllBytes().toString(Charset.defaultCharset())
    val entityConfig = yamlMapper.readValue(entityConfigBody, EntityConfig::class.java)

    entityConfig.entityList.forEach { entity->
        val entityRecordPath = "src/main/java/com/example/entity/${entity.recordName}.java"
        val entityRepositoryPath = "src/main/java/com/example/repository/${entity.recordName}Repository.java"
        val entityServicePath = "src/main/java/com/example/service/${entity.recordName}Service.java"
        println("Generating Entity ${entity.recordName}")
        File(entityRecordPath).writeText(
            generateEntity(entity),
            Charset.defaultCharset()
        )
        File(entityRepositoryPath).writeText(
            generateRepository(entity),
            Charset.defaultCharset()
        )
        File(entityServicePath).writeText(
            generateService(entity),
            Charset.defaultCharset()
        )
    }
}