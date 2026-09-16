package org.xaspire.tolink.agent;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class AgentScopeCompatibilityTest {

    @Test
    void coreLoadsAndMinimalAgentCanBeBuiltWithoutAProvider() {
        Model model = mock(Model.class);

        ReActAgent agent = ReActAgent.builder()
                .name("compatibility-test-agent")
                .sysPrompt("Compatibility test only")
                .model(model)
                .build();

        assertNotNull(agent);
    }
}
