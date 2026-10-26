package dev.irisconfigsearch.client;

import dev.irisconfigsearch.NeOculusOptionSource;
import dev.irisconfigsearch.ShaderOptionEntry;
import dev.irisconfigsearch.ShaderOptionSearchSession;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** First client UI for searching options from the currently active NeOculus shader pack. */
public final class ShaderOptionSearchScreen extends Screen {
    private static final int ROW_HEIGHT = 14;
    private ShaderOptionSearchSession session;
    private EditBox searchBox;
    private String status = "";
    private int scrollOffset;

    public ShaderOptionSearchScreen() {
        super(Component.translatable("screen.irisconfigsearch.title"));
        try {
            session = new ShaderOptionSearchSession(new NeOculusOptionSource());
        } catch (RuntimeException exception) {
            session = new ShaderOptionSearchSession(List::of);
            status = Component.translatable("screen.irisconfigsearch.integration_error").getString();
        }
        refreshOptions();
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 150;
        searchBox = new EditBox(this.font, left, 36, 205, 20,
                Component.translatable("screen.irisconfigsearch.search"));
        searchBox.setMaxLength(256);
        searchBox.setValue(session.query());
        searchBox.setResponder(value -> {
            session.setQuery(value);
            scrollOffset = 0;
        });
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("screen.irisconfigsearch.refresh"), button -> {
            refreshOptions();
            scrollOffset = 0;
        }).bounds(left + 211, 36, 89, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    private void refreshOptions() {
        try {
            session.refresh();
            if (status.isEmpty()) {
                status = session.results().isEmpty()
                        ? Component.translatable("screen.irisconfigsearch.no_options").getString()
                        : "";
            }
        } catch (RuntimeException exception) {
            status = Component.translatable("screen.irisconfigsearch.integration_error").getString();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);

        List<ShaderOptionEntry> results = session.results();
        int top = 66;
        int bottom = height - 42;
        int visibleRows = Math.max(0, (bottom - top) / ROW_HEIGHT);
        int maxOffset = Math.max(0, results.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxOffset);

        if (!status.isEmpty()) {
            graphics.drawCenteredString(font, status, width / 2, top + 10, 0xFF7777);
        } else if (results.isEmpty()) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.irisconfigsearch.no_matches"), width / 2, top + 10, 0xAAAAAA);
        } else {
            for (int i = 0; i < visibleRows && i + scrollOffset < results.size(); i++) {
                ShaderOptionEntry option = results.get(i + scrollOffset);
                int y = top + i * ROW_HEIGHT;
                String current = option.currentValue().map(value -> " = " + value).orElse("");
                String line = font.plainSubstrByWidth(option.name() + current, width - 24);
                graphics.drawString(font, line, 12, y, 0xE0E0E0, false);
                if (!option.description().isBlank()) {
                    graphics.drawString(font, font.plainSubstrByWidth(option.description(), width - 24),
                            12, y + 9, 0x909090, false);
                }
            }
        }

        graphics.drawCenteredString(font,
                Component.translatable("screen.irisconfigsearch.count", results.size()), width / 2, height - 40,
                0xB0B0B0);
        graphics.drawString(font, Component.translatable("screen.irisconfigsearch.scroll_hint"), 12,
                height - 40, 0x808080);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleRows = Math.max(0, (height - 42 - 66) / ROW_HEIGHT);
        int maxOffset = Math.max(0, session.results().size() - visibleRows);
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
