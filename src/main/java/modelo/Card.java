package modelo;

/**
 *
 * @author Jorge
 */
public class Card {
    String engName, spaName, attribute, type, text;    
    int level,attak,defense;

    public Card(String engName, String spaName, String attribute, String type, String text, int level, int attak, int defense) {
        this.engName = engName;
        this.spaName = spaName;
        this.attribute = attribute;
        this.type = type;
        this.text = text;
        this.level = level;
        this.attak = attak;
        this.defense = defense;
    }

    public Card() {
    }

    public String getEngName() {
        return engName;
    }

    public void setEngName(String engName) {
        this.engName = engName;
    }

    public String getSpaName() {
        return spaName;
    }

    public void setSpaName(String spaName) {
        this.spaName = spaName;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAttak() {
        return attak;
    }

    public void setAttak(int attak) {
        this.attak = attak;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }
    
    
}
