package org.xaspire.project.shared.id;

import java.util.UUID;

public final class UuidGenerator implements IdGenerator {
    @Override
    public UUID nextId() {
        return UUID.randomUUID();
    }
}
