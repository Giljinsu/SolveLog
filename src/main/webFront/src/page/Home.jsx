import Header from "../components/common/Header.jsx";
import PageList from "../components/PageList.jsx";
import {createContext, useContext, useEffect, useState} from "react";
import {useSearchContext} from "../context/SearchContext.jsx";
import {useLocation, useNavigate} from "react-router-dom";

const Home = () => {
  // const [postList, setPostList] = useState([])
  // const [isLoading, setIsLoading] = useState(false)
  const {searchPostList, isReady, getNextPostPage, postList, getPageByTagId} = useSearchContext();
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isReady) return;

    const getPostList = async () => {
      // setIsLoading(true);
      if (location.state?.tagId) {
        await getPageByTagId(location.state.tagId);
        // state 제거
        navigate(location.pathname, { replace: true });
      } else {
        await searchPostList();
      }
      // setIsLoading(false);
      // setPostList([...postList, ...newPostList]);
      // setPostList([...postList, newPostList]);

    }

    getPostList();
  }, [isReady]);



  useEffect(() => {
    const scrollEvent = () => {
      const scrollY = window.scrollY;
      const windowHeight = window.innerHeight;
      const rootHeight = document.getElementById("root").offsetHeight;

      if (scrollY + windowHeight >= rootHeight- 100) {
        getNextPostPage();
      }
    }

    document.addEventListener("scroll", scrollEvent);

    return () => {
      document.removeEventListener("scroll", scrollEvent);
    }
  }, []);

  // if (isLoading) return (<div>로딩중</div>)

  // if (postList) return (<div>로딩중</div>)

  return (
      <>
        <Header
            menu={true}
        />
        {
          postList.length > 0 ? (
          <PageList postList={postList}/>
          ) : (
              <div
                  style={{
                    "textAlign":"center",
                    "marginTop":"10rem"
                  }}
              >
                작성된 게시글이 없습니다.</div>
          )}
      </>
  )
}

export default Home;