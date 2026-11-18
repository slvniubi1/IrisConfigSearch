package dev.irisconfigsearch.client;

import dev.irisconfigsearch.NeOculusOptionSource;
import dev.irisconfigsearch.ShaderOptionEntry;
import dev.irisconfigsearch.ShaderOptionSearchSession;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Search and edit screen for options exposed by the active NeOculus shader pack. */
public final class ShaderOptionSearchScreen extends Screen {
    private static final int ROW_HEIGHT = 34;
    private static final int LIST_TOP = 78;
    private static final int LIST_BOTTOM_MARGIN = 42;
    // The per-row "Change" button: its size and position inside a row. Used by both
    // rendering and click handling so the clickable area matches what is drawn.
    private static final int ACTION_WIDTH = 86;
    private static final int ACTION_RIGHT_MARGIN = 8;
    private static final int ACTION_TOP = 5;
    private static final int ACTION_HEIGHT = 22;
    private static final String[] TYPE_FILTERS = {"all", "boolean", "string", "define"};
    private static final Boolean[] DEFAULT_FILTERS = {null, true, false};

    private ShaderOptionSearchSession session;
    private NeOculusOptionSource optionSource;
    private EditBox searchBox;
    /** Blocks the option list (integration error, no options found). */
    private String status = "";
    /** One-line message shown above the list without hiding it (apply result or edit error). */
    private String notice = "";
    private boolean noticeIsError;
    private int scrollOffset;
    private int typeFilterIndex;
    private int defaultFilterIndex;
    private final Map<String, String> pendingChanges = new LinkedHashMap<>();
    private Button applyButton;
    private Button discardButton;

    public ShaderOptionSearchScreen() {
        super(Component.translatable("screen.irisconfigsearch.title"));
        try {
            optionSource = new NeOculusOptionSource();
            session = new ShaderOptionSearchSession(optionSource);
        } catch (RuntimeException exception) {
            session = new ShaderOptionSearchSession(List::of);
            status = Component.translatable("screen.irisconfigsearch.integration_error").getString();
        }
        refreshOptions();
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 150;
        searchBox = new EditBox(this.font, left, 36, 104, 20,
                Component.translatable("screen.irisconfigsearch.search"));
        searchBox.setMaxLength(256);
        searchBox.setValue(session.query());
        searchBox.setResponder(value -> {
            session.setQuery(value);
            scrollOffset = 0;
        });
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(typeFilterLabel(), button -> {
            typeFilterIndex = (typeFilterIndex + 1) % TYPE_FILTERS.length;
            session.setTypeFilter(TYPE_FILTERS[typeFilterIndex]);
            button.setMessage(typeFilterLabel());
            scrollOffset = 0;
        }).bounds(left + 106, 36, 74, 20).build());

        addRenderableWidget(Button.builder(defaultFilterLabel(), button -> {
            defaultFilterIndex = (defaultFilterIndex + 1) % DEFAULT_FILTERS.length;
            session.setEnabledByDefault(DEFAULT_FILTERS[defaultFilterIndex]);
            button.setMessage(defaultFilterLabel());
            scrollOffset = 0;
        }).bounds(left + 182, 36, 72, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.irisconfigsearch.refresh"), button -> {
            clearNotice();
            refreshOptions();
            scrollOffset = 0;
        }).bounds(left + 256, 36, 44, 20).build());

        applyButton = addRenderableWidget(Button.builder(applyLabel(), button -> applyPending())
                .bounds(width / 2 - 154, height - 30, 100, 20).build());
        discardButton = addRenderableWidget(
                Button.builder(Component.translatable("screen.irisconfigsearch.discard"), button -> discardPending())
                        .bounds(width / 2 - 50, height - 30, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 + 54, height - 30, 100, 20).build());
        updateActionButtons();
    }

    private Component typeFilterLabel() {
        return Component.translatable("screen.irisconfigsearch.filter",
                Component.translatable("screen.irisconfigsearch.filter." + TYPE_FILTERS[typeFilterIndex]));
    }

    private Component defaultFilterLabel() {
        String key = defaultFilterIndex == 0 ? "all"
                : defaultFilterIndex == 1 ? "enabled" : "disabled";
        return Component.translatable("screen.irisconfigsearch.default_filter",
                Component.translatable("screen.irisconfigsearch.default_filter." + key));
    }

    private Component applyLabel() {
        return Component.translatable("screen.irisconfigsearch.confirm_save_count", pendingChanges.size());
    }

    private void updateActionButtons() {
        if (applyButton != null) {
            applyButton.active = !pendingChanges.isEmpty() && optionSource != null;
            applyButton.setMessage(applyLabel());
        }
        if (discardButton != null) {
            discardButton.active = !pendingChanges.isEmpty();
        }
    }

    private void applyPending() {
        if (pendingChanges.isEmpty() || optionSource == null) {
            return;
        }
        try {
            optionSource.applyChanges(pendingChanges);
            pendingChanges.clear();
            refreshOptions();
            setNotice(Component.translatable("screen.irisconfigsearch.applied").getString(), false);
        } catch (RuntimeException exception) {
            setNotice(Component.translatable("screen.irisconfigsearch.apply_error", describe(exception))
                    .getString(), true);
        }
        updateActionButtons();
    }

    private void discardPending() {
        pendingChanges.clear();
        clearNotice();
        refreshOptions();
    }

    private void setNotice(String text, boolean error) {
        notice = text;
        noticeIsError = error;
    }

    private void clearNotice() {
        notice = "";
    }

    /** Exception messages can be null; fall back to the exception type so the UI never shows "null". */
    private static String describe(RuntimeException error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    private void refreshOptions() {
        status = "";
        try {
            session.refresh();
            if (session.results().isEmpty()) {
                status = Component.translatable("screen.irisconfigsearch.no_options").getString();
            }
        } catch (RuntimeException exception) {
            status = Component.translatable("screen.irisconfigsearch.integration_error").getString();
        }
        updateActionButtons();
    }

    private int visibleRows() {
        return Math.max(0, (height - LIST_BOTTOM_MARGIN - LIST_TOP) / ROW_HEIGHT);
    }

    private int actionLeft() {
        return width - ACTION_RIGHT_MARGIN - ACTION_WIDTH;
    }

    private boolean isEditable(ShaderOptionEntry option) {
        return optionSource != null
                && ("boolean".equals(option.type()) || "string".equals(option.type()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);
        renderNotice(graphics);

        List<ShaderOptionEntry> results = session.results();
        int visibleRows = visibleRows();
        int maxOffset = Math.max(0, results.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxOffset);

        if (!status.isEmpty()) {
            graphics.drawCenteredString(font, status, width / 2, LIST_TOP + 10, 0xFF7777);
        } else if (results.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.irisconfigsearch.no_matches"), width / 2, LIST_TOP + 10, 0xAAAAAA);
        } else {
            int actionLeft = actionLeft();
            for (int i = 0; i < visibleRows && i + scrollOffset < results.size(); i++) {
                ShaderOptionEntry option = results.get(i + scrollOffset);
                int y = LIST_TOP + i * ROW_HEIGHT;
                String typeLabel = Component.translatable("screen.irisconfigsearch.type", option.type()).getString();
                graphics.drawString(font,
                        font.plainSubstrByWidth(option.name() + " [" + typeLabel + "]", actionLeft - 18),
                        12, y, 0xE0E0E0, false);

                if (!option.description().isBlank()) {
                    graphics.drawString(font, font.plainSubstrByWidth(option.description(), actionLeft - 18),
                            12, y + 10, 0x909090, false);
                }

                String currentValue = pendingChanges.getOrDefault(option.name(), option.currentValue().orElse(""));
                String currentLabel = currentValue.isEmpty() ? ""
                        : Component.translatable("screen.irisconfigsearch.current", currentValue).getString();
                String defaultLabel = option.defaultValue()
                        .map(value -> Component.translatable("screen.irisconfigsearch.default", value).getString())
                        .orElse("");
                String values = currentLabel.isEmpty() ? defaultLabel
                        : defaultLabel.isEmpty() ? currentLabel : currentLabel + "  |  " + defaultLabel;
                if (!values.isEmpty()) {
                    graphics.drawString(font, font.plainSubstrByWidth(values, actionLeft - 18),
                            12, y + 20, 0xA0C8FF, false);
                }

                boolean editable = isEditable(option);
                int color = editable ? 0xFF477A9F : 0xFF555555;
                graphics.fill(actionLeft, y + ACTION_TOP, width - ACTION_RIGHT_MARGIN,
                        y + ACTION_TOP + ACTION_HEIGHT, color);
                boolean staged = pendingChanges.containsKey(option.name());
                graphics.drawCenteredString(font,
                        Component.translatable(!editable ? "screen.irisconfigsearch.read_only"
                                : staged ? "screen.irisconfigsearch.staged" : "screen.irisconfigsearch.change"),
                        actionLeft + ACTION_WIDTH / 2, y + 12, 0xFFFFFF);
            }
        }

        graphics.drawCenteredString(font,
                Component.translatable("screen.irisconfigsearch.count", results.size()), width / 2, height - 40,
                0xB0B0B0);
        graphics.drawString(font, Component.translatable("screen.irisconfigsearch.scroll_hint"), 12,
                height - 40, 0x808080);
    }

    /** Shows the latest notice, or a reminder that staged changes are not active yet. */
    private void renderNotice(GuiGraphics graphics) {
        String text;
        int color;
        if (!notice.isEmpty()) {
            text = notice;
            color = noticeIsError ? 0xFF7777 : 0x77DD77;
        } else if (!pendingChanges.isEmpty()) {
            text = Component.translatable("screen.irisconfigsearch.pending_hint", pendingChanges.size()).getString();
            color = 0xFFD27A;
        } else {
            return;
        }
        graphics.drawCenteredString(font, font.plainSubstrByWidth(text, width - 24), width / 2, 62, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        // Rows are only drawn when there is no blocking status message.
        if (button != 0 || optionSource == null || !status.isEmpty()) {
            return false;
        }
        if (mouseX < actionLeft() || mouseX > width - ACTION_RIGHT_MARGIN || mouseY < LIST_TOP) {
            return false;
        }

        // Only react inside the drawn button, and only for rows that are actually visible.
        int row = (int) ((mouseY - LIST_TOP) / ROW_HEIGHT);
        double offsetInRow = (mouseY - LIST_TOP) - (double) row * ROW_HEIGHT;
        if (row >= visibleRows() || offsetInRow < ACTION_TOP || offsetInRow >= ACTION_TOP + ACTION_HEIGHT) {
            return false;
        }

        int index = row + scrollOffset;
        List<ShaderOptionEntry> results = session.results();
        if (index >= results.size()) {
            return false;
        }

        ShaderOptionEntry option = results.get(index);
        if (!isEditable(option)) {
            return false;
        }
        try {
            String shown = pendingChanges.getOrDefault(option.name(), option.currentValue().orElse(""));
            String next = optionSource.nextValue(option, shown);
            if (next.equals(option.currentValue().orElse(""))) {
                pendingChanges.remove(option.name());
            } else {
                pendingChanges.put(option.name(), next);
            }
            clearNotice();
            updateActionButtons();
        } catch (RuntimeException exception) {
            setNotice(Component.translatable("screen.irisconfigsearch.apply_error", describe(exception))
                    .getString(), true);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxOffset = Math.max(0, session.results().size() - visibleRows());
        scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) Math.signum(scrollY) * 3));
        return true;
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
