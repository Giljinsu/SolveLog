package com.study.blog.dto.post;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class SearchCondition {
    private String searchValue;
    private SearchType searchType; // TITLE, CONTENT, USERNAME, TITLE_AND_CONTENT
    private SearchOrderType searchOrderType;
    private String categoryType;
    private String username;
    private Boolean isTemp;
//    private Long tagId;
    private List<Long> tagIdList;
    private List<String> tagNameList;

    public SearchCondition(String searchValue, SearchType searchType, SearchOrderType searchOrderType) {
        this.searchValue = searchValue;
        this.searchType = searchType;
        this.searchOrderType = searchOrderType;
    }

    public SearchCondition(String searchValue, SearchType searchType,
        SearchOrderType searchOrderType,
        String categoryType) {
        this.searchValue = searchValue;
        this.searchType = searchType;
        this.searchOrderType = searchOrderType;
        this.categoryType = categoryType;
    }

}
