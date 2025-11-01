import Header from "../components/common/Header.jsx";
import PostContent from "../components/post/PostContent.jsx";
import {useLocation, useNavigate, useParams} from "react-router-dom";
import {useContext, useEffect, useState} from "react";
import axios from "../context/axiosInstance.js";
import {useAuth} from "../context/AuthContext.jsx";
import {LoginDispatchContext} from "../App.jsx";
import {useSearchContext} from "../context/SearchContext.jsx";
import {usePopup} from "../context/PopupContext.jsx";
import Loading from '../components/loading/Loading.jsx';
import LoadingPopup from "../components/loading/LoadingPopup.jsx";

const Post = () => {
  //
  const [postDetail, setPostDetail] = useState("")
  const location = useLocation();
  const {postId} = location.state || {}
  const {user, isLoading} = useAuth();
  const {resetSearchCondition} = useSearchContext();
  const nav = useNavigate();
  const [targetCommentIdForScroll, setTargetCommentIdForScroll] = useState(''); // 알림으로 이동하는경우 해당 댓글이 대댓글인 경우 대댓글창 열기
  // const [curComment, setCurComment] = useState({
  //   postId : postId,
  //   // userId : "",
  //   username : "",
  //   comment : ""
  // });
  const {setIsLoginOpen} = useContext(LoginDispatchContext)

  const confirm = usePopup(); // popup

  useEffect(() => {
    const hash = location.hash;
    if (hash) {
      // 약간 딜레이 주면 이미지/댓글 렌더 후 위치 정확
      setTimeout(() => {
        const el = document.querySelector(hash);
        if (el) {
          el.scrollIntoView({ behavior: "smooth", block: "center" });
          nav(location.pathname, {
            state : {
              postId : postId
            },
            replace: true
          });
        }
      }, 100); // 필요한 경우 조절
    }
    setTargetCommentIdForScroll(hash);
    // 해시 제거
  }, [location]);

  useEffect(() => {
    if (isLoading) return ; // 인증

    if (user) {
      // setCurComment({...curComment, username : user.username})
    }

    addViewCnt();
  }, [isLoading]);

  // 상세조회
  const getPostDetail = async () => {
    try {
      if(!postId) return;
      let api = await axios.get(`/api/getPostDetail/${postId}`);
      setPostDetail(api.data.data);
    } catch (e) {
      console.log(e)
    }
  }

  // 조회수 증가
  const addViewCnt = async () => {
    try {
      // 중복 조회수 방지
      const viewedPosts = localStorage.getItem("viewedPosts") || [];
      if (!viewedPosts.includes(postId)) {
        //
        const axiosResponse = await axios.post(`/api/posts/${postId}/view`);
        localStorage.setItem("viewedPosts", postId);

        // setPostDetail({...postDetail, viewCount : axiosResponse.data.viewCount})
      }
    } catch (e) {
      console.log(e);
    } finally {
      await getPostDetail();
    }
  }

  //댓글 리스트 조회
  const getCommentList = async () => {
    try {
      const response = await axios.get(`/api/getComments/${postId}`);

      setPostDetail({...postDetail,
        commentCount : response.data.data.length > 0 ? response.data.data[0].commentCnt : 0,
        comments : response.data.data
      })

    } catch (e) {
      console.log(e);
    }
  }

  //댓글 입력 시
  // const onCommentChange = (e) => {
  //   const name = e.target.name;
  //   const value = e.target.value;
  //
  //
  //   setCurComment({ ...curComment, [name] : value})
  // }
  const onCommentChange = (comment, setComment) => (e) => {
    const { name, value } = e.target;


    setComment({ ...comment, [name] : value})
  }


  //댓글 작성
  // const onClickCommentCreate = async () => {
  //   try {
  //     await axios.post("/api/createComment",curComment)
  //   } catch (e) {
  //     // console.log(e)
  //
  //     setIsLoginOpen(true);
  //   } finally {
  //     setCurComment({...curComment, comment: ""})
  //     await getCommentList();
  //   }
  //
  // }
  const onClickCommentCreate = async (comment, setComment) => {
    try {
      if (!user) {
        setIsLoginOpen(true);
        return;
      }

      await axios.post("/api/createComment",comment)
      setComment({...comment, comment: ""})
      await getCommentList();
    } catch (e) {
      // console.log(e)

    } finally {
    }

  }

  // 댓글 삭제
  const onClickCommentDelete = async (commentId) => {
    try {
      if (!(await viewPopup(
          "댓글삭제",
          "댓글을 삭제하시겠습니까?"
      ))) return;

      await axios.post(`/api/deleteComment/${commentId}`)
    } catch (e) {
      console.log(e)
    } finally {
      await getCommentList();
    }
  }

  // 좋아요 갯수 조회
  const getLikesCount= async () => {
    try {
      const axiosResponse = await axios.get(`/api/getLikesCount/${postId}`);

      setPostDetail({...postDetail, likeCount: axiosResponse.data})
    } catch (e) {
      console.log(e);
    }
  }

  // 게시글 삭제
  const deletePost = async (postId) => {
    try {
      if (!(await viewPopup(
          "게시글삭제",
          "게시글을 삭제하시겠습니까?"
      ))) return;

      await axios.post(`/api/deletePost/${postId}`)

      resetSearchCondition();
      nav("/");
    } catch (e) {
      alert("게시글을 삭제하는데 문제가 발생했습니다.")
    }

  }

  const viewPopup = async (header, body) => {
    return await confirm({
      header:header,
      body:body,
      leftButtonText:"취소",
      rightButtonText:"확인"
    })
  }

  // if (!postDetail) return (<Loading />)
  if (!postDetail) return (<LoadingPopup />)


  return (
      <>
          <Header/>
          <PostContent
              postId={postId}
              title={postDetail.title}
              author={postDetail.nickName}
              authorImg={postDetail.userImg}
              authorBio={postDetail.authorBio}
              postUsername={postDetail.username}
              writtenDate={postDetail.createdDate}
              content={postDetail.content}
              likes={postDetail.likeCount}
              commentCnt={postDetail.commentCount}
              comments={postDetail.comments}
              viewCount={postDetail.viewCount}
              // tags={postDetail.tags}
              tags={postDetail.tagList}
              files={postDetail.files}
              onCommentChange={onCommentChange}
              onClickCommentCreate={onClickCommentCreate}
              onClickCommentDelete={onClickCommentDelete}
              getLikesCount={getLikesCount}
              deletePost={deletePost}
              targetCommentIdForScroll={targetCommentIdForScroll}
          />
      </>
  )
}

export default Post;