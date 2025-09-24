import UserImg from "../common/UserImg.jsx";
import "./MyPageTitle.css"
import {useEffect, useRef, useState} from "react";
import {usePopup} from "../../context/PopupContext.jsx";
import axios from "../../context/axiosInstance.js";
import {useAuth} from "../../context/AuthContext.jsx";
import axiosInstance from "../../context/axiosInstance.js";
import {useNavigate} from "react-router-dom";


// isMyPage : 해당 페이지의 유저와 내가 로그인한 유저가 같은지 여부
const MyPageTitle = ({isMyPage, nickname, username, resetMyPage}) => {
  const inputRef = useRef(null);
  const confirm = usePopup();
  const [isEdit, setIsEdit] = useState(false);
  const isEditRef = useRef('');
  const nicknameRef = useRef(null);
  const [nicknameState, setNicknameState] = useState("");
  const nicknameSectionRef = useRef('');
  const [userImg, setUserImg] = useState({
    id:'',
    src:''
  })
  const {reFetchUser} = useAuth();
  const nav = useNavigate();
  // 백엔드 url
  //.env 파일에 서버 주소 저장
  const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;



  useEffect(() => {
    getUserInfo();
  }, []);

  useEffect(() => {
    setNicknameState(nickname);
    document.addEventListener('mousedown', handler);

    return ()=> document.removeEventListener("mousedown", handler);
  }, [nickname]);

  useEffect(() => {
    isEditRef.current = isEdit;
  }, [isEdit]);

  // 닉네임 변경시 다른 곳 클릭하면 편집 종료
  const handler = (e) => {
    const popup = document.querySelector(".Popup"); // 팝업 최상단 DOM
    if (popup && popup.contains(e.target)) return; // 🔥 팝업 클릭은 무시


    if (isEditRef.current && nicknameSectionRef.current &&
        !nicknameSectionRef.current.contains(e.target)) {
      if (nickname !== nicknameRef.current) nicknameRef.current.innerText = nickname;
      setIsEdit(false);
    }
  }


  const openPopup = async (header, body) => {
    return await confirm({
      header: header,
      body: body,
      leftButtonText: "아니요",
      rightButtonText: "예"
    });
  }

  // 이미지 클릭시
  const openFilePicker = async (e) => {
    if (!isMyPage) return;

    if (!(await openPopup("유저이미지 업로드", "이미지를 변경하시겠습니까?"))) return;

    e.stopPropagation();
    e.preventDefault();
    inputRef.current?.click();

  };

  // 이미지업로드 및 mdImg
  const uploadImage = async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("username", username)

    try {
      return await axios.post("/api/uploadUserImg",formData);
    } catch (e) {
      alert("이미지 업로드 실패");
    }
  }

  // 이미지 삭제
  const deleteUserImg = async (imgId) => {
    if (!imgId) return;
    try {
      await axios.post(`api/deleteFile/${imgId}`);
    } catch (e) {
      console.log(e);
    }
  }

  // 유저 이미지 변경 시
  const onChangeUserImg = async (e) => {
    const res = await uploadImage(e.target.files[0]);

    const fileId = res.data.fileId;


    // const mdImage = `![](${backendBaseUrl}/api/inlineFile/${fileId})\n`;
    const imgSrc = `${backendBaseUrl}/api/inlineFile/${fileId}`
    setUserImg({id:fileId, src: imgSrc});

    reFetchUser();

  }

  // 유저 정보
  const getUserInfo = async () => {
    try {
      const axiosResponse = await axios.get(`api/getUserImg/${username}`);
      const findUserImg = axiosResponse.data;

      const fileId = findUserImg.fileId;
      const imgSrc = `${backendBaseUrl}/api/inlineFile/${fileId}`;
      setUserImg({id: fileId, src: imgSrc })

    } catch (e) {
      console.log(e);
    }
  }

  // 닉네임 변경 버튼
  const onEditNickname = () => {
    if (!nicknameRef.current) return;
    const element = nicknameRef.current;

    const range = document.createRange();
    const selection = window.getSelection();

    // 끝에 커서를 놓기
    range.selectNodeContents(element);
    range.collapse(false); // false = 끝으로 이동

    selection.removeAllRanges();
    selection.addRange(range);

  }

  const onSubmitNickname = async (e) => {
    try {
      if (nicknameRef.current.innerText === nickname) {
        // nicknameRef.current.focus();
        setIsEdit(!isEdit);
        return;
      }
      if (!(await openPopup("닉네임 변경", "닉네임을 변경하시겠습니까?"))) return;

      await axiosInstance.post("/api/updateUser", {
        username: username,
        nickName: nicknameRef.current.innerText,
        role: "USER"
      });

      await reFetchUser();
      setNicknameState(nicknameRef.current.innerText);
      setIsEdit(!isEdit);
      nav(`/myPage/${username}`, {
        replace : true,
        state:{
          nickname:nicknameRef.current.innerText
        }
      })
      nickname = nicknameSectionRef.current.innerText;
    } catch (e) {

    }
  }

  return (
      <div className={"my-page-user-img-section"}>
        <UserImg
            radius={150}
            isAuthentication={isMyPage}
            nickname={nickname}
            userImg={userImg.id ? userImg.src : ""}
            onClickImg={isMyPage ? openFilePicker : resetMyPage}
        />
        <div ref={nicknameSectionRef} className={"my-page-user-nickname-section"}>

          <div
          //     className={`${isMyPage
          // && "my-page-user-nickname-authorized"}`}
              className={`my-page-user-nickname ${isEdit ? "my-page-user-nickname-edit" : ""}`}
              ref={nicknameRef}
              contentEditable={isEdit ? "true" : "false"}
              suppressContentEditableWarning={true}
              onClick={()=>resetMyPage()}

          >
            {nicknameState ? nicknameState : "닉네임오류"}
          </div>
          {isMyPage ? (
            <div className={"nickname-edit-icon-section"}>
              {!isEdit ? (
                  <svg
                      onClick={()=> {
                        setIsEdit(!isEdit)

                        setTimeout(() => {
                          onEditNickname();
                        },0);

                      }}
                      className={"nickname-edit-icon"}
                      xmlns="http://www.w3.org/2000/svg" width="14" height="14"
                      fill="none" stroke="currentColor"
                      viewBox="0 0 24 24">
                    <path d="M12 20h9"/>
                    <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z"/>
                  </svg>
              ) : (
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                       xmlns="http://www.w3.org/2000/svg"
                       className={"nickname-edit-icon"}
                       onClick={() => {
                         onSubmitNickname();

                       }}
                  >
                    <path d="M20 6L9 17L4 12" stroke="black"/>
                  </svg>
              )}



            </div>
          ) : ""}
        </div>

        <input type={"file"}
               ref={inputRef}
               accept={"image/jpeg, image/png, image/bmp, image/webp"}
               style={{display: "none"}}
               onChange={(e) => {
                 if (userImg.id !== '') deleteUserImg(userImg.id);
                 onChangeUserImg(e);
               }}
        />
      </div>
  )
}

export default MyPageTitle