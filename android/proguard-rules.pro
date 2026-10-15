# Project-specific R8 rules for the release build.
# Libraries (Compose, Room, kotlinx.serialization, AndroidX) ship their own consumer rules,
# so only add narrow keeps here when R8 reports them in build/outputs/mapping/release/missing_rules.txt.
