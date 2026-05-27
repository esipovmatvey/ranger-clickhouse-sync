package rangerclickhousesync.engine.builder.policy;

import rangerclickhousesync.dto.RangerPolicyDto;

import java.util.List;

/**
 * Контракт для классов, генерирующих SQL-команды ClickHouse на основе политики Ranger.
 */
public interface PolicySqlBuilder {
    List<String> build(RangerPolicyDto policy);
}