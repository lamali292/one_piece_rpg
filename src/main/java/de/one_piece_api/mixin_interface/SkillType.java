package de.one_piece_api.mixin_interface;

import java.util.Locale;

public enum SkillType {
    NONE,
    DEVIL_FRUIT,
    CLASS,
    SKILL_TREE;

    public static SkillType getType(String type) {
        try {
            return SkillType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return SKILL_TREE;
        }
    }
}
