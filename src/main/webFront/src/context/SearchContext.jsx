import {
  createContext,
  use,
  useContext,
  useEffect,
  useRef,
  useState
} from "react";
import axios from "axios";
import {useLocation, useNavigationType} from "react-router-dom";
import useCategoryList from "../hooks/useCategoryList.jsx";
import {options} from "../utils/SearchOptions.js"
import qs from "qs";

export const SearchContext = createContext();

export const SearchProvider = ({children}) => {
  const [searchCondition, setSearchCondition] = useState({
    searchValue: "",
    searchType: "TITLE",
    searchOrderType: "",
    categoryType: "",
    tagNameList: []
  })
  const [isReady, setIsReady] = useState(false)
  const [nextPage, setNextPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [postList, setPostList] = useState([])
  const hasNextRef = useRef(hasNext);
  const location = useLocation();
  const navigationType = useNavigationType();

  // 2025-07-29 : 검색조건 초기화시 게시글 카테고리중 첫번째요소를 자동으로 선택하기 위함
  const [categoryReloadKey, setCategoryReloadKey] = useState(0);
  const reloadCategories = () => setCategoryReloadKey(k => k + 1);
  const {categoryList, loading} = useCategoryList("SEARCH_CATEGORY", categoryReloadKey);


  const searchPostList = async () => {
    if (!isReady) return [];

    try {
      const axiosResponse = await axios.get("/api/getPostList", {
        params: {
          ...searchCondition,
          page: nextPage,
          size: 20,
        },
        paramsSerializer: params =>
            qs.stringify(params, {arrayFormat: "repeat"})
      });

      setHasNext(axiosResponse.data.hasNext);

      setPostList([...postList, ...axiosResponse.data.content || []]);

      // return axiosResponse.data.content || [];
    } catch (e) {
      console.log(e);
      return [];
    } finally {
      setNextPage(prev => prev + 1);
      setIsReady(false);
    }
  }

  const setSearchInput = (name, value) => {
    const updated = {...searchCondition, [name]: value};
    if (name === "categoryType") { // 카테고리 변경시 입력값 초기화
      updated.searchValue = "";
      updated.tagNameList = [];
    }

    // 태그 검색
    if (updated.searchValue !== "") {
      const result = parseSearchInput(updated.searchValue);
      setSearchCondition({
        ...updated,
        tagNameList: result.tagNameList,
        searchValue: result.searchValue ? result.searchValue : ""
      });
    } else {
      setSearchCondition(updated);
    }
    if (
        updated.categoryType &&
        updated.searchOrderType &&
        updated.searchType
    ) {
      setNextPage(0);
      setPostList([])

      setIsReady(true)
    }

  }

  const parseSearchInput = (input) => {
    if (!input) return;

    const tags = input.match(/#\S+/g);

    if (tags && tags.length > 0) {
      return {tagNameList: tags.map(tag => tag.slice(1))};
    } else {
      return {searchValue: input};
    }

  }

  const resetSearchCondition = () => {
    if (loading) return;
    const defaultCondition = {
      searchValue: "",
      searchType: "TITLE",
      searchOrderType: options[0],
      categoryType: categoryList[0].type,
    };
    setPostList([]);
    setNextPage(0);
    setSearchCondition(defaultCondition);
    setIsReady(true); // 초기화 시 검색이 안되는 현상으로 집어놓음
  };

  // 태그검색용 초기화
  const resetSearchConditionForTag = () => {
    if (loading) return;
    const defaultCondition = {
      searchValue: "",
      searchType: "TITLE",
      searchOrderType: options[0],
      categoryType: searchCondition.categoryType,
    };
    setPostList([]);
    setNextPage(0);
    setSearchCondition(defaultCondition);
    setIsReady(true); // 초기화 시 검색이 안되는 현상으로 집어놓음
  };


  const getNextPostPage = async () => {
    if (!hasNextRef.current) return;
    // setNextPage(prev => prev+1);

    setIsReady(true);
  }

  const getPageByTagId = async (tagId) => {
    if (!tagId || tagId === "") return;

    const tagIdList = [tagId];

    try {
      const axiosResponse = await axios.get("/api/getPostList", {
        params: {
          searchValue: "",
          searchType: "TITLE",
          searchOrderType: options[0],
          categoryType: searchCondition.categoryType,
          // tagId: tagId,
          tagIdList: tagIdList,
          page: 0,
          size: 20,
        },
        paramsSerializer: params =>
            qs.stringify(params, {arrayFormat: "repeat"})

        // qs 라이브러리
        // 해당 라이브러리로 arrayFormat: "repeat" 을 주게되면
        ///api/getPostList?tagIdList=1&tagIdList=2&tagIdList=3
        // 같이 찍힘
      });

      setNextPage(0);
      setPostList(axiosResponse.data.content);
      setHasNext(axiosResponse.data.hasNext);
      setIsReady(false);
    } catch (e) {
      console.log(e);
    }
  }

  useEffect(() => {
    if (!loading && categoryList.length > 0) {
      resetSearchCondition();
    }
  }, [categoryList, loading]);

  useEffect(() => {
    // console.log(navigationType)
    // 처음 불러올때 POP이 발생하는 문제가 있어서 두반 검색을 실행함
    if (navigationType === "POP") { // 뒤로가기 감지
      setIsReady(false);
      // setIsReady(true)
      // resetSearchCondition();
    }
  }, [location]);

  useEffect(() => {
    hasNextRef.current = hasNext;
  }, [hasNext]);

  return (
      <SearchContext.Provider
          value={{
            searchPostList,
            setSearchInput,
            setIsReady,
            isReady,
            searchCondition,
            resetSearchCondition,
            resetSearchConditionForTag,
            hasNext,
            setNextPage,
            getNextPostPage,
            postList,
            getPageByTagId,
            reloadCategories,
          }}>
        {children}
      </SearchContext.Provider>
  )
}

export const useSearchContext = () => {
  const searchContext = useContext(SearchContext);
  if (!searchContext) {
    throw new Error("useSearchContext SearchProvider 내부에서 사용되어야 합니다.")
  }

  return searchContext;
}