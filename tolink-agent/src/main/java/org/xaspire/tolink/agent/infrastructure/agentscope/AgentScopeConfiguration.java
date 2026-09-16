package org.xaspire.tolink.agent.infrastructure.agentscope;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xaspire.tolink.agent.infrastructure.configuration.AgentProperties;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "tolink.agent", name = "enabled", havingValue = "true")
public class AgentScopeConfiguration {

    @Bean
    ReActAgent toLinkAgent(AgentProperties properties, Model model) {
        return ReActAgent.builder()
                .name(properties.getName())
                .sysPrompt(properties.getSystemPrompt())
                .model(model)
                .build();
    }
}
