import noImg from '../../assets/Logo-Test.png';
import test from '../../assets/test2.webp'
import "./UserImg.css";
import {useEffect, useState} from "react";

const UserImg = ({userImg, nickname, radius, onClickImg, isAuthentication}) => {
  const [imgSrcError, setImgSrcError] = useState(false)
  radius = radius ? radius : 100;


  // 이미지 없을 경우 아바타 서클 텍스트
  const getAvatarCircleText = (nickname) => {
    if (!nickname) return ;
    const regex = ""
    // 특수문자 제외
    const filteredText = nickname.replace("/[^가-힣a-zA-Z]/g","");

    // .test 정규식 true false 반환
    // 한글 2글자
    if (/^[가-힣]+$/.test(filteredText)) {
      return filteredText.slice(0,2);
    }

    // 영어 2글자
    if (/^[a-zA-Z]+$/.test(filteredText)) {
      return filteredText.slice(0,2).toUpperCase();
    }

    //혼합 2글자
    return filteredText.slice(0,2);

  }

  return (
      <div>
        {!imgSrcError && userImg ? (
            <img
                onClick={()=>onClickImg ? onClickImg() : ""}
                className={`${isAuthentication ? "user-img-authorized"
                    : "user-img"}`}
                src={userImg ? userImg : test}
                alt={"이미지 없음"}
                width={radius}
                height={radius}
                onError={()=> setImgSrcError(true)}
            />
        ) : (
            <div>
              <div
                  className={`avatar-circle ${isAuthentication
                      ? "avatar-circle-authorized" : ""}`}
                  style={{width: `${radius}px`, height: `${radius}px`}}
                  onClick={(e)=>onClickImg ? onClickImg(e) : ""}
              >{getAvatarCircleText(nickname)}
              </div>
            </div>
        )}

        {/*<input type={"file"}*/}
        {/*       ref={inputRef}*/}
        {/*       accept={"image/jpeg, image/png, image/bmp, image/webp"}*/}
        {/*       style={{display: "none"}}*/}
        {/*       // onChange={(e) => {*/}
        {/*       //   if (thumbnail) deleteThumbnailFile(thumbnail.fileId)*/}
        {/*       //   onChangeThumbnail(e);*/}
        {/*       // }}*/}
        {/*/>*/}
      </div>
  )

}

export default UserImg;