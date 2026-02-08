package com.blaybus.backend.domain.content.dto.response;

import com.blaybus.backend.domain.content.Memo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class MemoResponse {

    @Getter
    @Builder
    public static class Item {
        private Long memoId;
        private String content;
        private LocalDateTime createdAt;

        public static Item from(Memo memo) {
            return Item.builder()
                    .memoId(memo.getId())
                    .content(memo.getContent())
                    .createdAt(memo.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ListResult {
        private List<Item> items;

        public static ListResult from(List<Memo> memos) {
            return ListResult.builder()
                    .items(memos.stream().map(Item::from).toList())
                    .build();
        }
    }
}
