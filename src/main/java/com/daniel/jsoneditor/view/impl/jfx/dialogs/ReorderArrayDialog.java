package com.daniel.jsoneditor.view.impl.jfx.dialogs;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
// Platform.runLater is not required by this class after timeline handler changes


/**
 * Dialog for reordering array elements via drag and drop.
 * Supports multi-select drag, visual drop indicators, and smooth proportional auto-scrolling.
 */
public class ReorderArrayDialog extends ThemedDialog<List<Integer>>
{
    private static final double CELL_HEIGHT = 32.0;
    private static final double MAX_HEIGHT = 600.0;
    private static final double DEFAULT_WIDTH = 800.0;
    private static final int PREVIEW_TRUNCATE = 140;

    // auto-scroll tuning: smooth pixel-based scrolling with proportional speed
    private static final double SCROLL_MARGIN = 50.0;
    private static final Duration SCROLL_INTERVAL = Duration.millis(25);
    private static final double MAX_SCROLL_SPEED = 0.008;

    private final ListView<ArrayItemWrapper> listView;
    private int insertionIndex = -1;
    private final List<Integer> draggingIndices = new ArrayList<>();

    private final Timeline autoScrollTimeline;
    private double lastMouseY = -1;

    /**
     * Create a dialog that allows reordering the given JSON array node.
     *
     * @param arrayNode the array node to reorder
     */
    public ReorderArrayDialog(JsonNode arrayNode)
    {
        super();
        setTitle("Reorder Array");
        setHeaderText("Drag items to reorder");
        setResizable(true);

        final List<ArrayItemWrapper> items = buildItems(arrayNode);

        listView = new ListView<>();
        listView.getItems().addAll(items);
        listView.getStyleClass().add("reorder-list");
        listView.setCellFactory(lv -> new ReorderCell());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setFixedCellSize(CELL_HEIGHT);
        listView.setPrefHeight(Math.min(items.size() * CELL_HEIGHT + 4, MAX_HEIGHT));
        listView.setPrefWidth(DEFAULT_WIDTH);
        listView.setMinHeight(200);
        VBox.setVgrow(listView, Priority.ALWAYS);

        autoScrollTimeline = new Timeline(new KeyFrame(SCROLL_INTERVAL, ev -> handleAutoScrollTick()));
        autoScrollTimeline.setCycleCount(Timeline.INDEFINITE);

        listView.setOnDragOver(event -> {
            if (event.getDragboard().hasString())
            {
                event.acceptTransferModes(TransferMode.MOVE);
                lastMouseY = event.getY();
                updateInsertionIndexFromMouseY();
                ensureAutoScroll();
            }
            event.consume();
        });

        listView.setOnDragDropped(event -> {
            final Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && insertionIndex >= 0)
            {
                final List<Integer> indices = parseIndices(db.getString());
                if (!indices.isEmpty())
                {
                    moveItems(indices, insertionIndex);
                    success = true;
                }
            }
            stopAutoScroll();
            clearDragState();
            event.setDropCompleted(success);
            event.consume();
        });

        final Label hint = new Label("Tip: Select multiple items with Ctrl/Shift, then drag as a group");
        hint.getStyleClass().add("dialog-empty-text");

        final VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        content.getChildren().addAll(listView, hint);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setPrefWidth(DEFAULT_WIDTH + 40);
        getDialogPane().setMinWidth(400);

        setResultConverter(button -> button == ButtonType.OK ? buildNewIndices() : null);
    }

    private ScrollBar getVerticalScrollBar()
    {
        for (final Node node : listView.lookupAll(".scroll-bar"))
        {
            if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL)
            {
                return sb;
            }
        }
        return null;
    }

    private void updateInsertionIndexFromMouseY()
    {
        final ScrollBar sb = getVerticalScrollBar();
        final int totalItems = listView.getItems().size();
        final int visibleCount = (int) Math.floor(listView.getHeight() / CELL_HEIGHT);
        int firstVisibleIndex = 0;
        if (sb != null && totalItems > visibleCount && sb.getMax() > sb.getMin())
        {
            final double scrollFraction = (sb.getValue() - sb.getMin()) / (sb.getMax() - sb.getMin());
            firstVisibleIndex = (int) Math.round(scrollFraction * (totalItems - visibleCount));
        }
        final int offsetInView = (int) Math.floor(lastMouseY / CELL_HEIGHT);
        final int newIndex = Math.clamp(firstVisibleIndex + offsetInView, 0, totalItems);
        if (newIndex != insertionIndex)
        {
            insertionIndex = newIndex;
            listView.refresh();
        }
    }

    private void ensureAutoScroll()
    {
        if (autoScrollTimeline == null || lastMouseY < 0)
        {
            stopAutoScroll();
            return;
        }
        final double h = listView.getHeight();
        if (lastMouseY < SCROLL_MARGIN || lastMouseY > h - SCROLL_MARGIN)
        {
            if (autoScrollTimeline.getStatus() != Animation.Status.RUNNING)
            {
                autoScrollTimeline.play();
            }
        }
        else
        {
            stopAutoScroll();
        }
    }

    private void stopAutoScroll()
    {
        if (autoScrollTimeline != null && autoScrollTimeline.getStatus() == Animation.Status.RUNNING)
        {
            autoScrollTimeline.stop();
        }
    }

    private void handleAutoScrollTick()
    {
        if (lastMouseY < 0)
        {
            return;
        }
        final ScrollBar sb = getVerticalScrollBar();
        if (sb == null)
        {
            return;
        }
        final double h = listView.getHeight();
        double scrollDelta = 0;

        if (lastMouseY < SCROLL_MARGIN)
        {
            // proportional: closer to top edge = faster scroll up
            final double ratio = 1.0 - (lastMouseY / SCROLL_MARGIN);
            scrollDelta = -ratio * MAX_SCROLL_SPEED;
        }
        else if (lastMouseY > h - SCROLL_MARGIN)
        {
            // proportional: closer to bottom edge = faster scroll down
            final double ratio = (lastMouseY - (h - SCROLL_MARGIN)) / SCROLL_MARGIN;
            scrollDelta = ratio * MAX_SCROLL_SPEED;
        }

        if (scrollDelta != 0)
        {
            final double range = sb.getMax() - sb.getMin();
            final double newValue = Math.clamp(sb.getValue() + scrollDelta * range, sb.getMin(), sb.getMax());
            sb.setValue(newValue);
            updateInsertionIndexFromMouseY();
        }
    }

    private List<ArrayItemWrapper> buildItems(JsonNode arrayNode)
    {
        final List<ArrayItemWrapper> out = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++)
        {
            out.add(new ArrayItemWrapper(i, arrayNode.get(i)));
        }
        return out;
    }

    private List<Integer> buildNewIndices()
    {
        return listView.getItems().stream()
                .map(item -> item.originalIndex)
                .collect(Collectors.toList());
    }

    private void clearDragState()
    {
        insertionIndex = -1;
        draggingIndices.clear();
        listView.refresh();
    }

    private static List<Integer> parseIndices(String csv)
    {
        final List<Integer> out = new ArrayList<>();
        if (csv == null || csv.isBlank())
        {
            return out;
        }
        for (final String part : csv.split(","))
        {
            try
            {
                out.add(Integer.parseInt(part.trim()));
            }
            catch (NumberFormatException ignored)
            {
            }
        }
        return out;
    }

    private void moveItems(List<Integer> fromIndices, int toIndex)
    {
        if (fromIndices.isEmpty())
        {
            return;
        }

        final List<ArrayItemWrapper> snapshot = new ArrayList<>(listView.getItems());
        final List<Integer> sorted = new ArrayList<>(fromIndices);
        sorted.sort(Integer::compareTo);

        final List<ArrayItemWrapper> moving = sorted.stream()
                .filter(i -> i >= 0 && i < snapshot.size())
                .map(snapshot::get)
                .collect(Collectors.toList());

        if (moving.isEmpty())
        {
            return;
        }

        int adjustedTarget = Math.clamp(toIndex, 0, listView.getItems().size());

        int itemsBeforeTarget = 0;
        for (final int idx : sorted)
        {
            if (idx < toIndex)
            {
                itemsBeforeTarget++;
            }
        }
        adjustedTarget -= itemsBeforeTarget;

        final List<Integer> descending = new ArrayList<>(sorted);
        descending.sort(Comparator.reverseOrder());
        for (final int idx : descending)
        {
            if (idx >= 0 && idx < listView.getItems().size())
            {
                listView.getItems().remove(idx);
            }
        }

        listView.getItems().addAll(adjustedTarget, moving);

        listView.getSelectionModel().clearSelection();
        for (int i = 0; i < moving.size(); i++)
        {
            listView.getSelectionModel().select(adjustedTarget + i);
        }
    }

    // -------------------- Data wrapper --------------------

    private static class ArrayItemWrapper
    {
        final int originalIndex;
        final String displayText;

        ArrayItemWrapper(int originalIndex, JsonNode node)
        {
            this.originalIndex = originalIndex;
            this.displayText = buildDisplayText(node);
        }

        private static String buildDisplayText(JsonNode node)
        {
            if (node.isObject())
            {
                return getObjectPreview(node);
            }
            else if (node.isArray())
            {
                return "[Array: " + node.size() + " items]";
            }
            else
            {
                final String text = node.asText();
                return text.length() > PREVIEW_TRUNCATE ? text.substring(0, PREVIEW_TRUNCATE - 3) + "..." : text;
            }
        }

        private static String getObjectPreview(JsonNode node)
        {
            final StringBuilder sb = new StringBuilder();
            final Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext() && sb.length() < PREVIEW_TRUNCATE)
            {
                final Map.Entry<String, JsonNode> field = it.next();
                if (sb.length() > 0)
                {
                    sb.append(" | ");
                }
                if (field.getValue().isValueNode())
                {
                    sb.append(field.getKey()).append(": ").append(field.getValue().asText());
                }
            }
            String result = sb.toString();
            if (result.length() > PREVIEW_TRUNCATE)
            {
                result = result.substring(0, PREVIEW_TRUNCATE - 3) + "...";
            }
            return result.isEmpty() ? "{...}" : result;
        }
    }

    // -------------------- List cell with drag support --------------------

    private class ReorderCell extends ListCell<ArrayItemWrapper>
    {
        private final Region topIndicator;
        private final Region bottomIndicator;
        private final Label indexLabel;
        private final Label textLabel;
        private final VBox cellContainer;

        ReorderCell()
        {
            topIndicator = new Region();
            topIndicator.getStyleClass().add("insertion-indicator");
            topIndicator.setPrefHeight(3);
            topIndicator.setMaxHeight(3);
            topIndicator.setVisible(false);

            bottomIndicator = new Region();
            bottomIndicator.getStyleClass().add("insertion-indicator");
            bottomIndicator.setPrefHeight(3);
            bottomIndicator.setMaxHeight(3);
            bottomIndicator.setVisible(false);

            indexLabel = new Label();
            indexLabel.getStyleClass().add("index-label");
            indexLabel.setMinWidth(28);

            textLabel = new Label();
            textLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(textLabel, Priority.ALWAYS);

            final HBox contentBox = new HBox(8, indexLabel, textLabel);
            contentBox.getStyleClass().add("reorder-cell-content");

            cellContainer = new VBox(topIndicator, contentBox, bottomIndicator);
            cellContainer.setFillWidth(true);

            setupDragHandlers();
        }

        private void setupDragHandlers()
        {
            setOnDragDetected(event -> {
                if (getItem() == null || isEmpty())
                {
                    return;
                }

                final List<Integer> selected = new ArrayList<>(listView.getSelectionModel().getSelectedIndices());
                if (!selected.contains(getIndex()))
                {
                    selected.clear();
                    selected.add(getIndex());
                    listView.getSelectionModel().clearAndSelect(getIndex());
                }

                draggingIndices.clear();
                draggingIndices.addAll(selected);
                draggingIndices.sort(Integer::compareTo);

                final Dragboard db = startDragAndDrop(TransferMode.MOVE);
                final ClipboardContent content = new ClipboardContent();
                content.putString(draggingIndices.stream().map(String::valueOf).collect(Collectors.joining(",")));
                db.setContent(content);

                event.consume();
            });

            setOnDragOver(event -> {
                if (!event.getDragboard().hasString())
                {
                    event.consume();
                    return;
                }

                event.acceptTransferModes(TransferMode.MOVE);

                lastMouseY = listView.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

                final double y = event.getY();
                final double h = getBoundsInLocal().getHeight();
                final int newInsertionIndex = (y < h / 2) ? getIndex() : getIndex() + 1;

                if (newInsertionIndex != insertionIndex)
                {
                    insertionIndex = Math.clamp(newInsertionIndex, 0, listView.getItems().size());
                    listView.refresh();
                }

                ensureAutoScroll();
                event.consume();
            });

            setOnDragDropped(event -> {
                final Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString() && insertionIndex >= 0)
                {
                    final List<Integer> indices = parseIndices(db.getString());
                    if (!indices.isEmpty())
                    {
                        moveItems(indices, insertionIndex);
                        success = true;
                    }
                }

                stopAutoScroll();
                clearDragState();
                event.setDropCompleted(success);
                event.consume();
            });

            setOnDragDone(event -> {
                stopAutoScroll();
                clearDragState();
                event.consume();
            });
        }

        @Override
        protected void updateItem(ArrayItemWrapper item, boolean empty)
        {
            super.updateItem(item, empty);

            if (empty || item == null)
            {
                setGraphic(null);
                setText(null);
                topIndicator.setVisible(false);
                bottomIndicator.setVisible(false);
            }
            else
            {
                indexLabel.setText(String.valueOf(item.originalIndex));
                textLabel.setText(item.displayText);

                final boolean showTop = (insertionIndex == getIndex());
                final boolean showBottom =
                        (insertionIndex == listView.getItems().size() && getIndex() == listView.getItems().size() - 1);

                topIndicator.setVisible(showTop);
                bottomIndicator.setVisible(showBottom);

                final boolean isDragged = draggingIndices.contains(getIndex());
                setOpacity(isDragged ? 0.4 : 1.0);

                setGraphic(cellContainer);
                setText(null);
            }
        }
    }

}
