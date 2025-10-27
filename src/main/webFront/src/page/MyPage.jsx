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
  const incomingNick = location.state.nickname || {};
  const [nickname, setNickname] = useState('');
  const [tabSelected, setTabSelected] = useState("my");
  const tabSelectedRef = useRef(tabSelected);
  const {isAuthentication, user, isLoading} = useAuth() || {};
  const username = params.username;

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
    getUserTagList();
    getUserPostList("전체보기");
  }, [username, user]);

  useEffect(()=> {
    tabSelectedRef.current = tabSelected;
    getTagList();
    onClickTag("전체보기");
  }, [tabSelected])

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

  useEffect(() => {
    setNickname(incomingNick);
  }, [incomingNick]);

  const getTagList = () => {
    if (tabSelected === "my") {
      getUserTagList();
    } else if (tabSelected === "like") {
      getLikeTagList();
    }
  }

  const getUserTagList = async () => {
    try {
      const axiosResponse = await axios.get(`api/getPostCountPerTagByUsername/${username}`);

      setTotalCount(axiosResponse.data.totalCount);
      setTagList(axiosResponse.data.tagCountDtos);

    } catch (e) {
      console.log(e);
    }
  }

  const getLikeTagList = async () => {
    try {
      const axiosResponse = await axios.get(`api/getLikePostCountPerTagByUsername/${username}`);

      setTotalCount(axiosResponse.data.totalCount);
      setTagList(axiosResponse.data.tagCountDtos);

    } catch (e) {
      console.log(e);
    }
  }

  const onClickTag = async (tagId) => {

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
          size: 20,
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
          size: 20,
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
            size: 20,
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
            size: 20,
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
              />
            }
            subHeader={
              <MyPageSearch onClickButton={searchPost}
                            isMyPage={isAuthentication && username
                                === user.username}
                            tabSelected={tabSelected}
                            setTabSelected={setTabSelected}
              />
            }
            listTemplate={postList && postList.map(post => (
                <MyPagePostList
                    key={post.id}
                    post={post}
                    onClickTag={onClickTag}
                    tabSelected={tabSelected}
                />
            ))}
            listPlaceholder={tabSelected === "my" ? "작성한 글이 없습니다."
                : "좋아요한 글이 없습니다."}
            leftMenuMax={<TagsMenu
                tagList={tagList}
                totalCount={totalCount}
                selected={tagSelected}
                onClickTag={onClickTag}
            />}
            leftMenuMin={<TagsMenuMin
                tagList={tagList}
                totalCount={totalCount}
                selected={tagSelected}
                onClickTag={onClickTag}
            />}
        />

        <div className={"post_end_buffer"} style={{"height": "80px"}}/>
      </div>
  )
}

export default MyPage;
