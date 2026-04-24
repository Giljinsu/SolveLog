import Header from "../components/common/Header.jsx";
import ListTemplate from "../components/template/ListTemplate.jsx";
import MyPageTitle from "../components/mypage/MyPageTitle.jsx";
import TagsMenu from "../components/common/TagsMenu.jsx";
import TagsMenuMin from "../components/common/TagsMenuMin.jsx";
import axios from "../context/axiosInstance.js";
import {useEffect, useRef, useState} from "react";
import {useLocation, useParams} from "react-router-dom";
import PostCard from "../components/common/PostCard.jsx";
import MyPagePostList from "../components/mypage/MyPagePostList.jsx";
import {useAuth} from "../context/AuthContext.jsx";
import {usePopup} from "../context/PopupContext.jsx";
import qs from "qs";
import MyPageSearch from "../components/mypage/MyPageSearch.jsx";
import axiosInstance from "../context/axiosInstance.js";
import useCategoryList from "../hooks/useCategoryList.jsx";
import MyPageStatistic from "../components/mypage/MyPageStatistic.jsx"

const MyPage = () => {
  const [tagList, setTagList] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const params = useParams();
  const [tagSelected, setTagSelected] = useState("전체보기");
  const tagSelectedRef = useRef(tagSelected);
  const [postList, setPostList] = useState([]);
  const postListRef = useRef(postList);
  // const [isSearchReady, setIsSearchReady] = useState(true);
  const isSearchReadyRef = useRef(true);
  // const [nextPage, setNextPage] = useState(0);
  const nextPageRef = useRef(0);
  const [hasNext, setHasNext] = useState(false);
  const hasNextRef = useRef(hasNext);
  const location = useLocation();
//   const incomingNick = location.state.nickname || {};
//   const incomingNick = location.state ? location.state.nickname : " ";
  const [nickname, setNickname] = useState('');
  const [tabSelected, setTabSelected] = useState("my");
  const {categoryList} = useCategoryList("SEARCH_CATEGORY");
  const [subTabList, setSubTabList] = useState([]); // 카테고리 ex 문제풀이, 자유게시판
  const [subTabSelected, setSubTabSelected] = useState("");
  const subTabSelectedRef = useRef(subTabSelected);
  const [searchValue, setSearchValue] = useState("");
  const [bioState, setBioState] = useState("");
  const [userImg, setUserImg] = useState({
      id:'',
      src:''
   });
  const tabSelectedRef = useRef(tabSelected);
  const isFirstSubTabEffect = useRef(true); // 처음 페이지 접속시 메인과 서브 탭으로 인해 요청 2번 보내는것을 방지
  const {isAuthentication, user} = useAuth() || {};
  const username = params.username;
  const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;
  const isStatistics = tabSelected === "statistics";
  const pageCnt = 5; // 페이지 수

  useEffect(() => {
    const scrollEvent = () => {
      const scrollY = window.scrollY;
      const windowHeight = window.innerHeight;
      const rootHeight = document.getElementById("root").offsetHeight;

      if (scrollY + windowHeight >= rootHeight- 100) {
        if (hasNextRef.current && isSearchReadyRef.current) {
          isSearchReadyRef.current = false;
          getNextPostPage();

        }
      }
    }

    document.addEventListener("scroll", scrollEvent);

    return () => {
      document.removeEventListener("scroll", scrollEvent);
    }
  }, []);

  useEffect(() => {
//     if(!subTabSelected) return; // 서브 탭 카테고리 세팅이 안되면 리턴
    if(!username) return;
    getUserInfo();
  }, [username, user]);

  const resetTagFilter = () => {
    setSearchValue("");

    if (tabSelected === "statistics") return;

    onClickTag("전체보기");
    getTagList();
  };

  useEffect(() => {
    tabSelectedRef.current = tabSelected;

    if (subTabSelected !== subTabList[0]) {
      setSubTabSelected(subTabList[0]);
      setSearchValue("");
      return;
    }
    if (tabSelected === "statistics") return;

    resetTagFilter();
  }, [tabSelected]);

  useEffect(() => {
    subTabSelectedRef.current = subTabSelected;

    if (isFirstSubTabEffect.current) {
      isFirstSubTabEffect.current = false;
      return;
    }

    resetTagFilter();
  }, [subTabSelected]);

  useEffect(()=> {
    const categoryTypes = categoryList.map(category => category.type);
    categoryTypes.unshift("전체");
    setSubTabList(categoryTypes);
    setSubTabSelected(categoryTypes[0]);
  }, [categoryList])

  useEffect(() => {
    hasNextRef.current = hasNext;
  }, [hasNext]);

  useEffect(()=> {
    tagSelectedRef.current = tagSelected;
  },[tagSelected])

  // useEffect(() => {
  //   nextPageRef.current = nextPage;
  // }, [nextPage]);

  useEffect(() => {
    postListRef.current = postList
  }, [postList]);

  // 유저 정보
  const getUserInfo = async () => {
    try {
      // const axiosResponse = await axios.get(`api/getUserImg/${username}`);
      const axiosResponse = await axios.get(`/api/getUser/${username}`);
      const findUserImg = axiosResponse.data.data.userImg;
      const userBio = axiosResponse.data.data.bio;
      const nickname = axiosResponse.data.data.nickname;

      // const fileId = findUserImg.fileId;
      const fileId = findUserImg !== null ? findUserImg.fileId : null;
      const imgSrc = `${backendBaseUrl}/api/inlineFile/${fileId}`;
      setUserImg({id: fileId, src: imgSrc })
      setNickname(nickname);

      setBioState(userBio ? userBio : '');
//       originBioRef.current = userBio;

    } catch (e) {
      console.log(e);
    }
  }

  const getTagList = () => {
    if (tabSelected === "my") {
      getUserTagList();
    } else if (tabSelected === "like") {
      getLikeTagList();
    }
  }

  const getUserTagList = async () => {
    try {
      let axiosResponse;
      if (subTabSelectedRef.current == "전체") {
        axiosResponse = await axios.get(`/api/getPostCountPerTagByUsername/${username}`);
      } else {
        axiosResponse = await axios.get(`/api/getPostCountPerTag`, {
           params : {
             // tagId: tagId !== "전체보기" ? tagId : "",
             username: username,
             categoryType: subTabSelectedRef.current
           },
           paramsSerializer : params =>
               qs.stringify(params, { arrayFormat: "repeat" })
        });
      }

      setTotalCount(axiosResponse.data.totalCount);
      setTagList(axiosResponse.data.tagCountDtos);

    } catch (e) {
      console.log(e);
    }
  }

  const getLikeTagList = async () => {
    try {
      let axiosResponse;
      if (subTabSelectedRef.current == "전체") {
        axiosResponse = await axios.get(`/api/getLikePostCountPerTagByUsername/${username}`);
      } else {
        axiosResponse = await axios.get(`/api/getLikePostCountPerTag`, {
           params : {
             // tagId: tagId !== "전체보기" ? tagId : "",
             username: username,
             categoryType: subTabSelectedRef.current
           },
           paramsSerializer : params =>
               qs.stringify(params, { arrayFormat: "repeat" })
        });
      }


      setTotalCount(axiosResponse.data.totalCount);
      setTagList(axiosResponse.data.tagCountDtos);

    } catch (e) {
      console.log(e);
    }
  }

  const onClickTag = async (tagId) => {
    setSearchValue("");
    if (tabSelected === "my") {
      getUserPostList(tagId);
    } else if (tabSelected === "like") {
      getLikePostLIst(tagId);
    }

  }

  const getLikePostLIst = async (tagId) => {
    setTagSelected(tagId);
    const tagIdList =  tagId !== "전체보기" ? [tagId] : ""
    try {
      const axiosResponse = await axios.get("/api/getLikesPostByUser", {
        params : {
          // tagId: tagId !== "전체보기" ? tagId : "",
          tagIdList: tagIdList,
          username: username,
          page: 0,
          size: pageCnt,
          ...(subTabSelectedRef.current !== "전체" && {
            categoryType: subTabSelectedRef.current
          })
        },
        paramsSerializer : params =>
            qs.stringify(params, { arrayFormat: "repeat" })
      });

      setPostList(axiosResponse.data.content);
      setHasNext(axiosResponse.data.hasNext);

      // setNextPage(1);
      nextPageRef.current = 1;
    } catch (e) {
      console.log(e);
    }
  }

  const getUserPostList = async (tagId) => {
    setTagSelected(tagId);
    const tagIdList =  tagId !== "전체보기" ? [tagId] : ""
    try {
      const axiosResponse = await axios.get("/api/getPostByTagIdAndUsername", {
        params : {
          // tagId: tagId !== "전체보기" ? tagId : "",
          tagIdList: tagIdList,
          username: username,
          page: 0,
          size: pageCnt,
          ...(subTabSelectedRef.current !== "전체" && {
            categoryType: subTabSelectedRef.current
          })
        },
        paramsSerializer : params =>
            qs.stringify(params, { arrayFormat: "repeat" })
      });

      setPostList(axiosResponse.data.content);
      setHasNext(axiosResponse.data.hasNext);

      // setNextPage(1);
      nextPageRef.current = 1;
    } catch (e) {
      console.log(e);
    }
  }

  const getNextPostPage = async () => {
    // setNextPage(prev => prev+1);
    try {
      const tagIdList =  tagSelectedRef.current !== "전체보기" ? [tagSelectedRef.current] : ""
      let axiosResponse;
      if (tabSelectedRef.current === "my") {
        axiosResponse = await axios.get("/api/getPostByTagIdAndUsername", {
          params : {
            // tagId: tagSelected !== "전체보기" ? tagSelected : "",
            tagIdList: tagIdList,
            username: username,
            page: nextPageRef.current,
            size: pageCnt,
            ...(subTabSelectedRef.current !== "전체" && {
              categoryType: subTabSelectedRef.current
            })
          },
          paramsSerializer : params =>
              qs.stringify(params, { arrayFormat: "repeat" })
        });
      } else if (tabSelectedRef.current === "like") {
        axiosResponse = await axios.get("/api/getLikesPostByUser", {
          params : {
            // tagId: tagSelected !== "전체보기" ? tagSelected : "",
            tagIdList: tagIdList,
            username: username,
            page: nextPageRef.current,
            size: pageCnt,
            ...(subTabSelectedRef.current !== "전체" && {
              categoryType: subTabSelectedRef.current
            })
          },
          paramsSerializer : params =>
              qs.stringify(params, { arrayFormat: "repeat" })
        });
      }

      if (!axiosResponse) return;

      setPostList([...postListRef.current, ...axiosResponse.data.content]);
      setHasNext(axiosResponse.data.hasNext);

      // setNextPage(nextPageRef.current+1);
      nextPageRef.current = nextPageRef.current+1;
    } catch (e) {
      console.log(e);
    } finally {
      isSearchReadyRef.current = true;
    }
  }

  const resetMyPage = () => {
    getUserTagList();
    onClickTag("전체보기");
    setTabSelected("my");
  }

  const searchPost = async (value) => {
    const tagIdList =  tagSelected !== "전체보기" ? [tagSelected] : ""
    try {
      let axiosResponse;
      if (tabSelectedRef.current === "my") {
        axiosResponse = await axios.get("/api/getPostByTagIdAndUsername", {
          params : {
            // tagId: tagId !== "전체보기" ? tagId : "",
            searchValue: value,
            searchType: "TITLE",
            tagIdList: tagIdList,
            username: username,
            ...(subTabSelectedRef.current !== "전체" && {
              categoryType: subTabSelectedRef.current
            }),
            page: 0,
            size: 20,
          },
          paramsSerializer : params =>
              qs.stringify(params, { arrayFormat: "repeat" })
        });
      } else if (tabSelectedRef.current === "like") {
        axiosResponse = await axios.get("/api/getLikesPostByUser", {
          params : {
            // tagId: tagId !== "전체보기" ? tagId : "",
            searchValue: value,
            searchType: "TITLE",
            tagIdList: tagIdList,
            username: username,
            ...(subTabSelectedRef.current !== "전체" && {
              categoryType: subTabSelectedRef.current
            }),
            page: 0,
            size: 20,
          },
          paramsSerializer : params =>
              qs.stringify(params, { arrayFormat: "repeat" })
        });
      }

      setPostList(axiosResponse.data.content);
      setHasNext(axiosResponse.data.hasNext);

      // setNextPage(1);
      nextPageRef.current = 1;
    } catch (e) {
      console.log(e);
    }

  }

  // if (!user) {
  //   return (<div>loading</div>)
  // }
  if (!nickname) {
    return;
  }

  return (
      <div>
        <Header/>
        <ListTemplate
          header={
            <MyPageTitle
              isMyPage={isAuthentication && username === user.username}
              nickname={nickname}
              username={username}
              resetMyPage={resetMyPage}
              bioState={bioState}
              setBioState={setBioState}
              userImg={userImg}
              setUserImg={setUserImg}
            />
          }
          subHeader={
            <MyPageSearch
              onClickButton={searchPost}
              isMyPage={isAuthentication && username === user.username}
              tabSelected={tabSelected}
              setTabSelected={setTabSelected}
              subTabList={subTabList}
              subTabSelected={subTabSelected}
              setSubTabSelected={setSubTabSelected}
              searchValue={searchValue}
              setSearchValue={setSearchValue}
            />
          }
          {...(!isStatistics ? {
            listTemplate: postList?.map(post => (
              <MyPagePostList
                key={post.id}
                post={post}
                onClickTag={onClickTag}
                tabSelected={tabSelected}
              />
            )),
            listPlaceholder: tabSelected === "my" ? "작성한 글이 없습니다." : "좋아요한 글이 없습니다.",
            leftMenuMax: (
              <TagsMenu
                tagList={tagList}
                totalCount={totalCount}
                selected={tagSelected}
                onClickTag={onClickTag}
              />
            ),
            leftMenuMin: (
              <TagsMenuMin
                tagList={tagList}
                totalCount={totalCount}
                selected={tagSelected}
                onClickTag={onClickTag}
              />
            ),
          }: {
            listTemplate : [
              <MyPageStatistic
                key={0}
                username={username}
                categoryType={subTabSelected}
              />
            ]
          })}
        />

        <div className={"post_end_buffer"} style={{"height": "80px"}}/>
      </div>
  )
}

export default MyPage;
