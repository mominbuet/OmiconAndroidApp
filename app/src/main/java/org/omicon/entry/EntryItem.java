package org.omicon.entry;

public class EntryItem {
    String id, name, quantity, teacher, mobile, nick;

    public EntryItem(String i, String n, String q, String t, String m, String nick) {
        this.nick = nick;
        id = i;
        name = n;
        quantity = q;
        teacher = t;
        mobile = m;
    }
}
