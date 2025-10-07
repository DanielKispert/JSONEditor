package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.model.observe.Observer;
import com.daniel.jsoneditor.view.impl.jfx.toast.Toasts;


public interface View extends Observer
{
    void cantValidateJson();
    
    void selectJsonAndSchema();
    
    void showToast(Toasts toast);
    
    /**
     * Updates the window title with the given unsaved changes count.
     *
     * @param unsavedChangesCount number of unsaved changes
     */
    void updateWindowTitle(int unsavedChangesCount);
}
