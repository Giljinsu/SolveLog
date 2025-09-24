import Header from "../components/common/Header.jsx";
import ListTemplate from "../components/template/ListTemplate.jsx";
import testImage from "../assets/Logo-Test.png";
import TempList from "../components/templist/TempList.jsx";
import axios from "../context/axiosInstance.js";
import {useAuth} from "../context/AuthContext.jsx";
import {useEffect, useRef, useState} from "react";
import {useNavigate} from "react-router-dom";
import {usePopup} from "../context/PopupContext.jsx";

const TempPost = () => {
  const [tmpPostList, setTmpPostList] = useState([]);
  const [nextPage, setNextPage] = useState(0);
  const [hasNext, setHasNext] = useState(true);
  const hasNextRef = useRef(hasNext);
  const isFetchingRef = useRef(false);
  const nav = useNavigate();
  const {isLoading, user} = useAuth();

  const confirm = usePopup();

  useEffect(() => {
    if (!user) return;
    getTmpPostList(user.username);
  }, [isLoading]);

  useEffect(() => {
    const scrollEvent = () => {
      const scrollY = window.scrollY;
      const windowHeight = window.innerHeight;
      const rootHeight = document.getElementById("root").offsetHeight;

      if (scrollY + windowHeight >= rootHeight -100) {
        getTmpPostList(user.username);
      }
    }

    document.addEventListener("scroll", scrollEvent);

    return () => {
      document.removeEventListener("scroll", scrollEvent);
    }
  }, [user]);

  useEffect(() => {
    hasNextRef.current = hasNext;
  }, [hasNext]);

  const getTmpPostList = async (username) => {
    if (isFetchingRef.current || !hasNextRef.current) return;
    isFetchingRef.current = true;

    try {
      const axiosResponse = await axios.get(`/api/getTmpPostList/${username}`,{
        params : {
          page: nextPage,
          size: 20,
        },
      });

      setHasNext(axiosResponse.data.hasNext);

      setTmpPostList(prev =>[...prev, ...axiosResponse.data.content || []]);

      setNextPage(prev => prev+1);
    } catch (e) {
      console.log(e);
    } finally {
      isFetchingRef.current =false;
    }
  }

  const getImage = (imgId) => {
    if (!imgId) return null;

    const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;

    return`${backendBaseUrl}/api/inlineFile/${imgId}`;
  }

  const onClickList = (postId) => {
    nav("/postEdit", {
      state : {
        postId: postId
      }
    });
  }

  const deletePost = async (postId) => {
    try {
      if (!(await confirm({
        header:"임시 게시글 삭제",
        body:"게시글 삭제하시겠습니까?",
        leftButtonText:"취소",
        rightButtonText:"확인"
      }))) return;

      await axios.post(`api/deletePost/${postId}`);

      setTmpPostList(prev => prev.filter(post => post.id !== postId));
    } catch (e) {
      alert("게시글을 삭제하는데 문제가 발생했습니다.")
    }
  }



    return(
      <div>
        <Header menu={false} />
        <ListTemplate
            listTemplate={tmpPostList && tmpPostList.map(tmpPost =>
                <TempList
                    key={tmpPost.id}
                    postId={tmpPost.id}
                    title={tmpPost.title}
                    summary={tmpPost.summary}
                    listImg={getImage(tmpPost.thumbnailFile.fileId)}
                    createdDate={tmpPost.createdDate}
                    onClick={onClickList}
                    onDeleteClick={deletePost}
                />
            )}
            header={"임시작성글"}
            listPlaceholder={"임시 저장한 글이 없습니다."}
        />
      </div>
  )
}


export default TempPost;