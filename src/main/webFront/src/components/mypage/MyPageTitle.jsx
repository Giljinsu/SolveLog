import UserImg from "../common/UserImg.jsx";
import "./MyPageTitle.css"
import {useEffect, useRef, useState} from "react";
import {usePopup} from "../../context/PopupContext.jsx";
import axios from "../../context/axiosInstance.js";
import {useAuth} from "../../context/AuthContext.jsx";
import axiosInstance from "../../context/axiosInstance.js";
import {useNavigate} from "react-router-dom";


// isMyPage : 해당 페이지의 유저와 내가 로그인한 유저가 같은지 여부
const MyPageTitle = ({isMyPage, nickname, bio, username, resetMyPage}) => {
  const inputRef = useRef(null);
  const confirm = usePopup();
  const [isNicknameEdit, setIsNicknameEdit] = useState(false);
  const isNicknameEditRef = useRef('');
  const [isBioEdit, setIsBioEdit] = useState(false);
  const isBioEditRef = useRef('');
  const nicknameRef = useRef(null);
  const bioRef = useRef(null);
  const originBioRef = useRef('');
  const [nicknameState, setNicknameState] = useState("");
  const [bioState, setBioState] = useState("");
  const nicknameSectionRef = useRef('');
  const bioSectionRef = useRef('');
  const [userImg, setUserImg] = useState({
    id:'',
    src:''
  })
  const {reFetchUser} = useAuth();
  const nav = useNavigate();
  // 백엔드 url
  //.env 파일에 서버 주소 저장
  const backendBaseUrl = import.meta.env.VITE_API_BASE_URL;
  const [isBioPlaceholder, setIsBioPlaceholder] = useState(false);

  useEffect(() => {
    document.addEventListener('mousedown', bioHandler);
    document.addEventListener('mousedown', handler);


    getUserInfo();
    return ()=> {
      document.removeEventListener("mousedown", bioHandler);
      document.removeEventListener("mousedown", handler);
    }
  }, []);

  useEffect(() => {
    setNicknameState(nickname);
  }, [nickname]);


  useEffect(() => {
    isNicknameEditRef.current = isNicknameEdit;
  }, [isNicknameEdit]);

  useEffect(() => {
    isBioEditRef.current = isBioEdit;
  }, [isBioEdit]);

  useEffect(() => {
    if (!bioRef.current) return;
    if (bioRef.current.innerText === "바이오를 입력해주세요..") {
      setIsBioPlaceholder(true);
    } else {
      setIsBioPlaceholder(false);
    }
  }, [bioRef.current, bioState]);

  // 닉네임 변경시 다른 곳 클릭하면 편집 종료
  const handler = (e) => {
    const popup = document.querySelector(".Popup"); // 팝업 최상단 DOM
    if (popup && popup.contains(e.target)) return; // 팝업 클릭은 무시

    if (isNicknameEditRef.current && nicknameSectionRef.current &&
        !nicknameSectionRef.current.contains(e.target)) {
      if (nickname !== nicknameRef.current.innerText) nicknameRef.current.innerText = nickname;
      setIsNicknameEdit(false);
    }
  }

  // 바이오 변경시 다른 곳 클릭하면 편집 종료
  const bioHandler = (e) => {
    const popup = document.querySelector(".Popup"); // 팝업 최상단 DOM
    if (popup && popup.contains(e.target)) return; // 팝업 클릭은 무시

    if (isBioEditRef.current && bioSectionRef.current &&
        !bioSectionRef.current.contains(e.target)) {

      if (originBioRef.current !== bioRef.current.innerText) {
        bioRef.current.innerText = originBioRef.current ? originBioRef.current : "바이오를 입력해주세요..";
      }

      setIsBioEdit(false);
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
      // const axiosResponse = await axios.get(`api/getUserImg/${username}`);
      const axiosResponse = await axios.get(`/api/getUser/${username}`);
      const findUserImg = axiosResponse.data.data.userImg;
      const userBio = axiosResponse.data.data.bio;

      const fileId = findUserImg.fileId;
      const imgSrc = `${backendBaseUrl}/api/inlineFile/${fileId}`;
      setUserImg({id: fileId, src: imgSrc })

      setBioState(userBio);
      originBioRef.current = userBio;

    } catch (e) {
      console.log(e);
    }
  }

  // 닉네임 변경 버튼
  const onEdit = (curRef) => {
    if (!curRef.current) return;
    const element = curRef.current;

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
      const inputNickname = nicknameRef.current.innerText.trim();
      if (inputNickname === "") {
        nicknameRef.current.innerText = nickname;
        setIsNicknameEdit(!isNicknameEdit);
        return;
      }
      if (inputNickname === nickname) {
        // nicknameRef.current.focus();
        setIsNicknameEdit(!isNicknameEdit);
        return;
      }
      if (!(await openPopup("닉네임 변경", "닉네임을 변경하시겠습니까?"))) return;

      await axiosInstance.post("/api/updateUser", {
        username: username,
        nickName: inputNickname,
        role: "USER"
      });

      await reFetchUser();
      setNicknameState(inputNickname);
      setIsNicknameEdit(!isNicknameEdit);
      nav(`/myPage/${username}`, {
        replace : true,
        state:{
          nickname:inputNickname
        }
      })
      nickname = inputNickname;
    } catch (e) {

    }
  }

  const onSubmitBio= async (e) => {
    const innerBio = bioRef.current.innerText.trim();
    // 바이오는 없애고 싶을수 있으니
    try {
      // const originBio = bio ? bio : "";
      if (innerBio === originBioRef.current) {
        // nicknameRef.current.focus();
        if (originBioRef.current === "") {
          bioRef.current.innerText = "바이오를 입력해주세요.."
        }

        setIsBioEdit(!isBioEdit);
        return;
      }

      if (!(await openPopup("바이오 변경", "바이오를 변경하시겠습니까?"))) return;

      await axiosInstance.post("/api/updateUser", {
        username: username,
        bio: innerBio,
        role: "USER"
      });

      await reFetchUser();
      setBioState(innerBio);
      setIsBioEdit(!isBioEdit);
      nav(`/myPage/${username}`, {
        replace : true,
        state:{
          nickname:nicknameRef.current.innerText
        }
      })
      // nav(`/myPage/${username}`, {
      //   replace : true,
      //   state:{
      //     bio:bioRef.current.innerText
      //   }
      // })
      originBioRef.current = innerBio;
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
        <div className={"my-page-user-section"}>
          <div ref={nicknameSectionRef} className={"my-page-user-nickname-section"}>
            <div
            //     className={`${isMyPage
            // && "my-page-user-nickname-authorized"}`}
                className={`my-page-user-nickname ${isNicknameEdit ? "my-page-user-edit" : ""}`}
                ref={nicknameRef}
                contentEditable={isNicknameEdit ? "true" : "false"}
                suppressContentEditableWarning={true}
                onClick={()=>resetMyPage()}
            >
              {nicknameState ? nicknameState : "닉네임오류"}
            </div>
            {isMyPage ? (
              <div className={"nickname-edit-icon-section"}>
                {!isNicknameEdit ? (
                    <svg
                        onClick={()=> {
                          setIsNicknameEdit(!isNicknameEdit)

                          setTimeout(() => {
                            onEdit(nicknameRef);
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
          {/*바이오*/}
          {isMyPage ? (
            <div ref={bioSectionRef} className={"my-page-user-bio-section"}>
              <div
                  className={`my-page-user-bio ${isBioEdit ? "my-page-user-edit" : ""}`}
                  ref={bioRef}
                  contentEditable={isBioEdit ? "true" : "false"}
                  suppressContentEditableWarning={true}
                  onFocus={(e) => {
                    if (e.currentTarget.innerText === "바이오를 입력해주세요..") {
                      e.currentTarget.innerText = "";
                    }
                  }}
                  style={{
                    color : isBioPlaceholder ? isBioEdit ? "black" : "gray" : "black"
                  }}
              >
                {
                      bioState !== "" ? bioState : "바이오를 입력해주세요.."
                }
              </div>
              {isMyPage ? (
                  <div className={"bio-edit-icon-section"}>
                    {!isBioEdit ? (
                        <svg
                            onClick={()=> {
                              setIsBioEdit(!isBioEdit)

                              setTimeout(() => {
                                onEdit(bioRef);
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
                               onSubmitBio();

                             }}
                        >
                          <path d="M20 6L9 17L4 12" stroke="black"/>
                        </svg>
                    )}

                  </div>
              ) : ""}
            </div>
          ) : bioState ? (
              <div
                  //     className={`${isMyPage
                  // && "my-page-user-nickname-authorized"}`}
                  className={`my-page-user-bio ${isBioEdit
                      ? "my-page-user-bio-edit" : ""}`}
                  ref={bioRef}
                  contentEditable={isBioEdit ? "true" : "false"}
                  suppressContentEditableWarning={true}
                  // onClick={()=>resetMyPage()}
              >
                {
                  bioState
                }
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