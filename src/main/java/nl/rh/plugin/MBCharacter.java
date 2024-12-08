package nl.rh.plugin;

import javax.swing.*;

public enum MBCharacter {

    AUTO(Icons.AUTO);

    private final ImageIcon icon;

    MBCharacter(ImageIcon icon) {
        this.icon = icon;
    }

    public String getDisplayName() {
        return icon.getDescription();
    }
    public ImageIcon getIcon() {
        return icon;
    }

}