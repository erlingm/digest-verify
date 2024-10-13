package no.moldesoft.app.sha256;

enum Key {
    digest(OptionType.property), hash(OptionType.property), file(OptionType.property), debug(OptionType.flag);

    private final OptionType optionType;

    Key(OptionType optionType) {
        this.optionType = optionType;
    }

    public OptionType optionType() {
        return optionType;
    }
}
