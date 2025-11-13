package com.daniel.jsoneditor.view.impl.jfx.popups;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.daniel.jsoneditor.controller.Controller;
import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.view.impl.jfx.buttons.HistoryBookmarkButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.HistoryRevertButton;
import com.daniel.jsoneditor.view.impl.jfx.buttons.HistoryUndoButton;
import com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips.TooltipHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Floating popup window for browsing command history (browser-style navigation)
 */
public class HistoryPopup extends BasePopup<Void>
{
    private static final int MAX_VISIBLE_ITEMS = 20;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private final Controller controller;
    private final VBox historyList;
    
    public HistoryPopup(Controller controller)
    {
        super();
        this.controller = controller;
        
        this.historyList = new VBox(2);
        historyList.setPadding(new Insets(8));
        
        refreshHistoryList();
        
        final ScrollPane scrollPane = new ScrollPane(historyList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(400);
        scrollPane.setPrefHeight(300);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Use existing popup styling pattern
        scrollPane.getStyleClass().add("popup-list-view");
        
        popup.getContent().add(scrollPane);
    }
    
    @Override
    public void setItems(Void unused)
    {
        // Not needed for this popup, but required by BasePopup
        refreshHistoryList();
    }
    
    public void show(double x, double y)
    {
        refreshHistoryList();
        setPopupPosition(null, x, y);
        show();
    }
    
    @Override
    protected void moveVertically(double newHeight)
    {
        // Keep popup below the button instead of moving it above
        // BasePopup default behavior is: popup.setY(posY - newHeight)
        // We want: popup.setY(posY) to keep it below the button
        popup.setY(posY);
    }
    
    private void refreshHistoryList()
    {
        historyList.getChildren().clear();
        
        final List<CommandManager.HistoryEntry> undoHistory = controller.getCommandManager().getUndoHistory();
        final List<CommandManager.HistoryEntry> visibleEntries = getVisibleEntries(undoHistory);
        
        if (visibleEntries.isEmpty())
        {
            final Label emptyLabel = new Label("No command history available");
            emptyLabel.getStyleClass().add("history-empty-text");
            historyList.getChildren().add(emptyLabel);
            return;
        }
        
        for (final CommandManager.HistoryEntry entry : visibleEntries)
        {
            final HBox entryBox = createHistoryEntryBox(entry);
            historyList.getChildren().add(entryBox);
        }
    }
    
    private List<CommandManager.HistoryEntry> getVisibleEntries(List<CommandManager.HistoryEntry> allEntries)
    {
        // If we have 20 or fewer entries, show them all in chronological order
        if (allEntries.size() <= MAX_VISIBLE_ITEMS)
        {
            return new java.util.ArrayList<>(allEntries);
        }
        
        // For more than 20 entries: show most recent 20, but keep all bookmarked items
        final List<CommandManager.HistoryEntry> result = new java.util.ArrayList<>();
        int nonBookmarkedCount = 0;
        
        // Go through entries in chronological order (most recent first)
        for (CommandManager.HistoryEntry entry : allEntries)
        {
            if (entry.isBookmarked())
            {
                // Always include bookmarked entries
                result.add(entry);
            }
            else if (nonBookmarkedCount < MAX_VISIBLE_ITEMS)
            {
                // Include non-bookmarked entries up to the limit
                result.add(entry);
                nonBookmarkedCount++;
            }
            // Skip non-bookmarked entries beyond the limit
        }
        
        return result;
    }
    
    private HBox createHistoryEntryBox(CommandManager.HistoryEntry entry)
    {
        final HBox entryBox = new HBox(8);
        entryBox.setAlignment(Pos.CENTER_LEFT);
        entryBox.setPadding(new Insets(4));
        entryBox.getStyleClass().add("history-entry");
        
        // Command info
        final VBox infoBox = createCommandInfoBox(entry);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        // Action buttons
        final HBox buttonBox = createActionButtonBox(entry);
        
        entryBox.getChildren().addAll(infoBox, buttonBox);
        
        // Add tooltip with detailed information
        final String tooltipText = createTooltipText(entry);
        TooltipHelper.addTooltip(entryBox, tooltipText);
        
        // Add click handler to hide popup when action is taken
        entryBox.setOnMouseClicked(event -> {
            if (!event.getTarget().toString().contains("Button"))
            {
                hide();
            }
        });
        
        return entryBox;
    }
    
    private VBox createCommandInfoBox(CommandManager.HistoryEntry entry)
    {
        final VBox infoBox = new VBox(1);
        
        final Text commandLabel = new Text(entry.getCommand().getLabel());
        commandLabel.getStyleClass().add("history-command-text");
        
        final String timeText = formatTimestamp(entry.getTimestamp());
        final Text timeLabel = new Text(timeText);
        timeLabel.getStyleClass().add("history-time-text");
        
        infoBox.getChildren().addAll(commandLabel, timeLabel);
        
        if (entry.isBookmarked())
        {
            final Text bookmarkIndicator = new Text("★ Bookmarked");
            bookmarkIndicator.getStyleClass().add("history-bookmark-indicator");
            infoBox.getChildren().add(bookmarkIndicator);
        }
        
        return infoBox;
    }
    
    private HBox createActionButtonBox(CommandManager.HistoryEntry entry)
    {
        final HBox buttonBox = new HBox(3);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        final HistoryUndoButton undoButton = new HistoryUndoButton(entry);
        final HistoryRevertButton revertButton = new HistoryRevertButton(entry);
        final HistoryBookmarkButton bookmarkButton = new HistoryBookmarkButton(entry, this::refreshHistoryList);
        
        // Make buttons smaller and style for popup
        undoButton.getStyleClass().addAll("small-button", "popup-button");
        revertButton.getStyleClass().addAll("small-button", "popup-button");
        bookmarkButton.getStyleClass().addAll("small-button", "popup-button");
        
        // Override button actions to hide popup after execution
        undoButton.setOnMouseClicked(event -> {
            event.consume();
            controller.undo();
            hide();
        });
        
        revertButton.setOnMouseClicked(event -> {
            event.consume();
            controller.getCommandManager().revertToHistoryEntry(entry);
            hide();
        });
        
        // Bookmark button doesn't hide popup since it's just toggling state
        bookmarkButton.setOnMouseClicked(event -> {
            event.consume();
            entry.setBookmarked(!entry.isBookmarked());
            bookmarkButton.updateIcon();
            refreshHistoryList();
        });
        
        buttonBox.getChildren().addAll(undoButton, revertButton, bookmarkButton);
        
        return buttonBox;
    }
    
    private String createTooltipText(CommandManager.HistoryEntry entry)
    {
        final StringBuilder tooltip = new StringBuilder();
        tooltip.append("Command: ").append(entry.getCommand().getLabel()).append("\n");
        tooltip.append("Time: ").append(formatTimestamp(entry.getTimestamp())).append("\n");
        tooltip.append("Changes: ").append(entry.getChanges().size()).append(" modifications\n");
        
        if (entry.isBookmarked())
        {
            tooltip.append("Status: Bookmarked\n");
        }
        
        tooltip.append("\nActions:\n");
        tooltip.append("• Undo: Revert this single change\n");
        tooltip.append("• History: Revert to this point\n");
        tooltip.append("• Bookmark: ").append(entry.isBookmarked() ? "Remove bookmark" : "Save this version");
        
        return tooltip.toString();
    }
    
    private String formatTimestamp(long timestamp)
    {
        final LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        );
        return dateTime.format(TIME_FORMATTER);
    }
}


