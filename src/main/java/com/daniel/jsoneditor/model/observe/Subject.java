package com.daniel.jsoneditor.model.observe;

public interface Subject
{
    void registerObserver(Observer newObserver);

    void removeObserver(Observer observer);
    
    void notifyObservers();
}
