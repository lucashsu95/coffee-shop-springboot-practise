package tw.edu.ntub.imd.birc.coffeeshop.databaseconfig.entity.enumerate;

import lombok.Getter;
import tw.edu.ntub.birc.common.util.StringUtils;

import java.util.List;
import java.util.Map;

public enum UserRole {
    NO_PERMISSION(0, "訪客"),
    STUDENT(1, "學生"),
    ADMIN(2, "管理員"),
    TA(3, "助教"),
    PARENT_SYSTEM_ADMIN(4, "母系統管理員");

    @Getter
    private final Integer value;
    @Getter
    private final String typeName;

    UserRole(Integer value, String typeName) {
        this.value = value;
        this.typeName = typeName;
    }

    public static UserRole of(Integer value) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.getValue().equals(value)) {
                return userRole;
            }
        }
        return UserRole.NO_PERMISSION;
    }

    public static String getRoleTypeName(UserRole userRoleEnum) {
        return "[" + userRoleEnum.getTypeName() + "]";
    }

    public static boolean isStudent(String roleName) {
        return StringUtils.isEquals(roleName, getRoleTypeName(STUDENT));
    }

    public static boolean isNoPermission(String roleName) {
        return StringUtils.isEquals(roleName, getRoleTypeName(NO_PERMISSION)) ||
                StringUtils.isEquals(roleName, "[ROLE_ANONYMOUS]");
    }

    private static final Map<String, List<UserRole>> SYSTEM_ROLE_MAP = Map.of(
            "1", List.of(STUDENT, PARENT_SYSTEM_ADMIN),
            "2", List.of(STUDENT, TA),
            "3", List.of(STUDENT, ADMIN),
            "4", List.of(STUDENT, ADMIN)
    );

    public static List<UserRole> getRolesBySystemId(String systemId) {
        return SYSTEM_ROLE_MAP.getOrDefault(systemId, List.of(NO_PERMISSION));
    }
}


