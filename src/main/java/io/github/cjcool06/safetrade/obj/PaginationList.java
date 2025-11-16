package io.github.cjcool06.safetrade.obj;

import io.github.cjcool06.safetrade.utils.Text;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;

import java.util.List;

public class PaginationList extends ChatPaginator {
    private String title, header, footer;
    private String padding = "-";
    private List<String> contents;

    public static PaginationList builder() {
        return new PaginationList();
    }

    public PaginationList title(String title) {
        this.title = title;
        return this;
    }

    public PaginationList contents(List<String> contents) {
        this.contents = contents;
        return this;
    }

    public PaginationList header(String header) {
        this.header = header;
        return this;
    }

    public PaginationList footer(String footer) {
        this.footer = footer;
        return this;
    }

    public PaginationList padding(String padding) {
        this.padding = padding;
        return this;
    }

    private String getHeaderFooter(String headerFooter) {
        StringBuilder headerBuilder = new StringBuilder();
        if (headerFooter != null) {
            int fullHeaderLengthHalf = (GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - headerFooter.length()) / 2 - 1;
            StringBuilder headerHalfBuilder = new StringBuilder();
            for (int i = 0; i < fullHeaderLengthHalf; i++) {
                headerHalfBuilder.append(padding);
            }
            headerBuilder.append(headerHalfBuilder).append(" ").append(headerFooter).append(" ").append(headerHalfBuilder);
        } else {
            for (int i = 0; i < GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH; i++) {
                headerBuilder.append(padding);
            }
        }
        return headerBuilder.toString();
    }

    public String truncate(String string, int length) {
        if (string.length() > length) {
            return string.substring(0, length);
        }
        return string;
    }

    public NavigableChatPage build() {
        String header = getHeaderFooter(this.header);
        String contentsLine = String.join("\n", this.contents);
        BaseComponent[] pageNavigationBar = Text.builder().create();
        String footer = getHeaderFooter(this.footer);


        return new NavigableChatPage(paginate(contentsLine, 0), header, pageNavigationBar, footer);
    }

    public void sendTo(CommandSender src) {
        src.sendMessage(title);
    }

    public class NavigableChatPage extends ChatPaginator.ChatPage  {
        String header;
        BaseComponent[] pageNavigationBar;
        String footer;

        public NavigableChatPage(ChatPaginator.ChatPage page, String header, BaseComponent[] pageNavigationBar, String footer) {
            super(page.getLines(), page.getPageNumber(), page.getTotalPages());
            this.header = header;
            this.pageNavigationBar = pageNavigationBar;
            this.footer = footer;
        }
    }
}
