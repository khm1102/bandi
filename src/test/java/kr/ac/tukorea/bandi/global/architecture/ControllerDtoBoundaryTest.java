package kr.ac.tukorea.bandi.global.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerDtoBoundaryTest {

    private static final String DOMAIN_PACKAGE =
            "kr.ac.tukorea.bandi.domain";

    @Test
    void API_컨트롤러와_Swagger_계약은_model을_직접_참조하지_않는다()
            throws ClassNotFoundException {
        Set<Class<?>> contractTypes = searchRestControllers();
        Set<Class<?>> swaggerContracts = contractTypes.stream()
                .flatMap(type -> Set.of(type.getInterfaces()).stream())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        contractTypes.addAll(swaggerContracts);

        Set<String> violations = new LinkedHashSet<>();
        for (Class<?> contractType : contractTypes) {
            for (Method method : contractType.getDeclaredMethods()) {
                for (Class<?> parameterType : method.getParameterTypes()) {
                    if (isDomainModel(parameterType)) {
                        violations.add(contractType.getName() + "#"
                                + method.getName() + " -> "
                                + parameterType.getName());
                    }
                }
            }
        }

        assertThat(violations).isEmpty();
    }

    private Set<Class<?>> searchRestControllers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        Set<Class<?>> controllers = new LinkedHashSet<>();
        for (var beanDefinition : scanner.findCandidateComponents(DOMAIN_PACKAGE)) {
            controllers.add(Class.forName(beanDefinition.getBeanClassName()));
        }
        return controllers;
    }

    private boolean isDomainModel(Class<?> type) {
        Package typePackage = type.getPackage();
        return typePackage != null
                && typePackage.getName().startsWith(DOMAIN_PACKAGE)
                && typePackage.getName().contains(".model");
    }
}
