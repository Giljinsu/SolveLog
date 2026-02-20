import PostEditor from "../components/postedit/PostEditor.jsx";
import axios from "../context/axiosInstance.js";
import {useLocation, useNavigate} from "react-router-dom";
import {useSearchContext} from "../context/SearchContext.jsx";
import {useEffect, useRef, useState} from "react";
import {usePopup} from "../context/PopupContext.jsx";

const PostEdit = () => {
  const nav = useNavigate();
  const {resetSearchCondition} =useSearchContext();
  const [alarmList, setAlarmList] = useState([]);
  const [postDetail, setPostDetail] = useState({
    id: '',
    title: '',
    summary: '',
    content: '',
    categoryType: '',
    tags: '',
    files: '',
  });
  const alarmId = useRef(0);
  const location = useLocation();
  const {postId, isTempButtonVisible} = location.state || {};
  const confirm = usePopup();

  // 알람생성
  const createAlarm = (content, type) => {
    const newAlarmId = alarmId.current++;
    setAlarmList(prev => [
      ...prev,
      {
        id: newAlarmId,
        content: content,
        type: type
      }
    ]);

    closeAlarmByTimeout(newAlarmId);
  }

  // 알림 타이머
  const closeAlarmByTimeout = (id) => {
    setTimeout(()=>{
      closeAlarm(id)
    },3000)
  }

  //알림 닫기
  const closeAlarm = (id) => {
    setAlarmList(prev => prev.filter(alarm => alarm.id !== id));
  }

  // 확인창
  const openPopup = async (header, body) => {
    return await confirm({
      header: header,
      body: body,
      leftButtonText: "아니요",
      rightButtonText: "예"
    });

  }

  // 유효성 검사
  const chkValid = (initData) => {
    if (initData.title === "") {
      // alert("제목을 입력해주세요.");
      createAlarm("제목을 입력해주세요.", "bad");
      return false;
    } else if (initData.summary === "") {
      // alert("요약을 입력해주세요.")
      createAlarm("요약을 입력해주세요.", "bad");
      return false;
    } else if (initData.categoryType === "") {
      // alert("카테고리를 설정해주세요.");
      createAlarm("카테고리를 설정해주세요.", "bad");
      return false;
    }

    return true;

  }

  // 게시글 생성
  const createPost = async (initData) => {
    try {

      if (!chkValid(initData)) return;
      if (initData.isTemp === false && !(await openPopup(
          "게시글 작성",
          `게시글을 작성하시겠습니까?`
      ))) return;

      const axiosResponse = await axios.post("/api/createPost", initData);

      const newPostId = axiosResponse.data;
      if (initData.isTemp === false) {
        // resetSearchCondition();
        // nav("/")
        nav(`/post/${newPostId}/${initData.title}`, {
          state : {
            postId : newPostId
          }
        })
      } else { // 임시저장
        // const newPostId = axiosResponse.data;
        setPostDetail({...postDetail, postId : newPostId})
        createAlarm("임시저장 되었습니다.", "positive");
      }

    } catch (e) {
      createAlarm("저장에 실패하였습니다.", "bad");
    }

  }

  // 게시글 수정
  const updatePost = async (initData) => {
    try {
      if (!chkValid(initData)) return;
      if (initData.isTemp === false && !(await openPopup(
          "게시글 작성",
          `게시글을 작성하시겠습니까?`
      ))) return;

      await axios.post("/api/updatePost", initData);

      if (initData.isTemp === false) {
        // resetSearchCondition();
        // nav("/")
        nav(`/post/${initData.postId}/${initData.title}`, {
          state : {
            postId : initData.postId
          }
        })
      } else { // 임시저장
        createAlarm("임시저장 되었습니다.", "positive");
      }

    } catch (e) {
      createAlarm("저장에 실패하였습니다.", "bad");
    }
  }

  // 게시글 조회
  const getPostDetail = async () => {
    try {
      let api = await axios.get(`/api/getPostDetail/${postId ? postId : postDetail.postId}`);
      setPostDetail(api.data.data);
    } catch (e) {
      console.log(e)
    }
  }

  useEffect(() => {
    if (!postId && !postDetail.postId) return;


    getPostDetail();
  }, [postId, postDetail.postId]);


  return(
      <>
        <PostEditor
          createPost={createPost}
          alarmList={alarmList}
          setAlarmList={setAlarmList}
          alarmId={alarmId}
          closeAlarm={closeAlarm}
          postDetail={postDetail}
          updatePost={updatePost}
          isTempButtonVisible={isTempButtonVisible}
          createAlarm={createAlarm}
        />
      </>
  )
}

export default PostEdit;