package fr.mathilde.moreVanillaAdvancements.gui.selectors;

public interface SelectionCallback<T> {
    void onSelect(T selected);
    void onCancel();
}

