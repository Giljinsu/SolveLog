import "./Header.css"
import Dropdown from "./Dropdown.jsx";
import {useContext, useEffect, useRef, useState} from "react";
import Title from "./Title.jsx";
import {Button1, Button2} from "./Button.jsx";
import Categories from "./Categories.jsx";
import {LoginModal, SearchModal, AlarmModal} from "./Modals.jsx";
import {LoginContext, LoginDispatchContext} from "../../App.jsx";
import LoginIcon from "./LoginIcon.jsx";
import {useAuth} from "../../context/AuthContext.jsx";
import {useNavigate} from "react-router-dom";
import axios from "../../context/axiosInstance.js";
import axiosInstance from "../../context/axiosInstance.js";
import {usePopup} from "../../context/PopupContext.jsx";

// menu : 헤더 밑 카테고리 등 하위 메뉴들 표시 여부
const Header = ({menu}) => {
  const {isLoginOpen, isSearchOpen} = useContext(LoginContext);
  const {setIsLoginOpen, setIsSearchOpen} = useContext(LoginDispatchContext)
  const {user, isLoading, isAuthentication} = useAuth();
  const nav = useNavigate();
  const [alarmCount, setAlarmCount] = useState(0);
  const [isAlarmOpen, setIsAlarmOpen] = useState(false);
  const [alarmList, setAlarmList] = useState([]);
  const alarmRef = useRef('');
  const confirm = usePopup();
  // const [isLoginOpen, setIsLoginOpen] = useState(false);
  // const [isSearchOpen, setIsSearchOpen] = useState(false)

  const onLoginButtonClick = () => {
    setIsLoginOpen(true);
  }

  const onSearchButtonClick = () => {
    setIsSearchOpen(true);
  }

  const onAlarmButtonClick = () => {
    if (isAuthentication) {
      setIsAlarmOpen(!isAlarmOpen);
    } else {
      setIsLoginOpen(true);
    }
  }

  // 알림 조회
  const getAlarm = async () => {
    try {
      const axiosResponse = await axios.get(`/api/getAlarmList/${user.username}`);

      if (axiosResponse.data.data.length > 0) {
        setAlarmCount(axiosResponse.data.data[0].alarmCnt);
        setAlarmList(axiosResponse.data.data);
      } else {
        setAlarmCount(0);
        setAlarmList([]);
      }
    } catch (e) {
      console.log(e)
    }
  }

  // 알림 삭제
  const deleteAlarm = async (alarmId) => {
    try {
      await axiosInstance.post(`/api/deleteAlarm/${alarmId}`)
      getAlarm();
    } catch (e) {
      console.log(e);
    }
  }

  // 알림 모두 조회
  const updateAllAlarmIsViewed = async () => {
    try {
      await axiosInstance.post(`/api/updateAlarmsIsTrue/${user.username}`)
      getAlarm();
    } catch (e) {
      console.log(e);
    }
  }

  // 모든 알림 삭제
  const deleteAllAlarm = async  () => {
    try {
      if (!(await confirm({
        header:"알림삭제",
        body:"모든 알림을 삭제하시겠습니까?",
        leftButtonText:"취소",
        rightButtonText:"확인"
      }))) return;

      await axiosInstance.post(`/api/deleteAlarmsByUsername/${user.username}`)
      getAlarm();
    } catch (e) {
      console.log(e);
    }
  }

  useEffect(() => {
    if (!isAuthentication || isLoading) {
      setAlarmCount(0);
      return;
    }
    // if (!isLoading) return;
    // 알림 가져오기


    getAlarm();

  }, [isLoading ,isAuthentication, user]);

  useEffect(() => {

    const alarmModalHandler = (e) => {
      const Popup = document.querySelector(".Popup");

      if (alarmRef.current &&
          !alarmRef.current.contains(e.target) &&
          !(Popup && Popup.contains(e.target))
      ) {
        setIsAlarmOpen(false);
      }
    }

    document.addEventListener("mousedown", alarmModalHandler);


    return () => {
      document.removeEventListener("mousedown", alarmModalHandler);
    }
  }, []);


  return (
      <>
        <div className={"Header"}>
          <div className={"left_child"}><Title /></div>
          <div className={"center_child"}></div>
          <div className={"right_child"}>
            <div ref={alarmRef} className={"header-alarm-section"}>
              <Button1
                  buttonText={
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="24"
                        height="24"
                        fill="none"
                        stroke="black"
                        strokeWidth="1"
                        viewBox="0 0 24 24"
                    >
                      <path d="M12 22c1.1 0 2-.9 2-2H10c0 1.1.9 2 2 2z"/>
                      <path
                          d="M18 16v-5c0-3.07-1.63-5.64-4.5-6.32V4a1.5 1.5 0 00-3 0v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
                    </svg>
                  }
                  buttonEvent={onAlarmButtonClick}
              />
              {
                alarmCount > 0 && (
                    <div className={"header-alarm-count"}>
                      {alarmCount}
                    </div>
                  )
              }
              {isAlarmOpen &&
                  <AlarmModal
                      // alarmRef={alarmRef}
                      getAlarm={getAlarm}
                      alarmList={alarmList}
                      deleteAlarm={deleteAlarm}
                      updateAllAlarmIsViewed={updateAllAlarmIsViewed}
                      deleteAllAlarm={deleteAllAlarm}
                  />}
            </div>
            <Button1
                // buttonText={"🔍"}
                buttonText={
                  <svg xmlns="http://www.w3.org/2000/svg"
                       width="24"
                       height="24"
                       fill="none"
                       viewBox="0 0 24 24"
                       strokeWidth="1.5"
                       stroke="currentColor"
                       className="size-6">
                    <path strokeLinecap="round" strokeLinejoin="round"
                          d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z"/>
                  </svg>
                }
                buttonEvent={onSearchButtonClick}
            />

            {!isAuthentication ? (
                <Button2
                    buttonText={"로그인"}
                    buttonEvent={onLoginButtonClick}
                />
            ) : (
                <div className={"login_icon"} >
                <LoginIcon username={user.username} menu={menu} nickname={user.nickname}/>
              </div>
            )}
          </div>
        </div>
        {menu && (
          <div className={"menu"}>
            <div className={"left_menu"}>
              <Categories/>
            </div>
            <div className={"right_menu"}>
              {isAuthentication
                  && <button
                      className={"write_post_button"} onClick={()=>nav("/postEdit")}>
                    글 작성하기 </button>}
              <Dropdown/>
            </div>
          </div>
        )}
        {/*모달창 부분*/}
        {isLoginOpen && <LoginModal setIsLoginOpen={setIsLoginOpen}/>}
        {isSearchOpen && <SearchModal setIsSearchOpen={setIsSearchOpen}/>}
      </>
  )
}

export default Header;