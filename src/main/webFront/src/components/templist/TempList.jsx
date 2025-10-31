import noImage from "../../assets/no-image.png";
import "./TempList.css"
import relativeTime from 'dayjs/plugin/relativeTime';
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import dayjs from "dayjs";

const TempList = ({postId, listImg, title, summary, createdDate, onClick, onDeleteClick}) => {

  // dayjs.extend(utc)
  // dayjs.extend(timezone)
  dayjs.extend(relativeTime);
  dayjs.locale('ko'); // 한국어 설정

  // const now = dayjs().utc(); // 현재 시각
  // const postDate = dayjs(createdDate).utc();
  const now = dayjs(); // 현재 시각
  const postDate = dayjs(createdDate);

  return (
      <div className={"temp-list-section"}>
        <div  onClick={()=>onClick(postId)} className={"temp-list-img-section"}>
          <img
              alt={"이미지 없음"}
              src={listImg ? listImg : noImage}
          />
        </div>

        <div className={"temp-list-text-section"}>
          <div className={"temp-list-header"}>
            <div onClick={()=>onClick(postId)} className={"temp-list-title"}>
              {title}
            </div>
            <button
                className={"temp-list-delete-button"}
                onClick={()=>onDeleteClick(postId)}
            >
              삭제
            </button>
          </div>

          <div onClick={()=>onClick(postId)} className={"temp-list-body"}>
            {summary}
          </div>

          <div className={"temp-list-createdDate"}>
            {postDate.from(now)}
          </div>
        </div>


      </div>
  )
}

export default TempList;