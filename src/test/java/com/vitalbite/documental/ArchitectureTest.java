package com.vitalbite.documental;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.vitalbite.documental")
class ArchitectureTest {

    @ArchTest
    static final ArchRule core_must_not_depend_on_infrastructure_or_spring =
            noClasses().that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..infrastructure..",
                            "org.springframework..",
                            "jakarta.persistence..")
                    ;
}
