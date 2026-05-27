package rangerclickhousesync.engine.builder.policy;

import java.util.List;

/**
 * Базовый класс для билдеров, генерирующих SQL-команды по политике Ranger.
 * <p>
 * Содержит общую логику создания роли, добавления GRANT-ов и назначения роли пользователям
 * с активацией через {@code SET DEFAULT ROLE ALL}.
 */
public abstract class AbstractPolicySqlBuilder implements PolicySqlBuilder {
    protected void appendRoleWithGrants(
            final List<String> sql,
            final String roleName,
            final List<String> grantSqls,
            final List<String> users
    ) {
        sql.add("CREATE ROLE IF NOT EXISTS " + roleName);
        sql.addAll(grantSqls);
        users.forEach(user -> {
            sql.add("GRANT " + roleName + " TO " + user);
            sql.add("SET DEFAULT ROLE ALL TO " + user);
        });
    }
}
