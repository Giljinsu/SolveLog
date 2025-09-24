export const getKorDate = (date) => {

  if (!date) {
    return 'err'
  }

  if (!(date instanceof Date)) {
    date = new Date(date);
  }


  let year = date.getFullYear();
  let month = date.getMonth()+1;
  let day = date.getDate();


  return `${year}년 ${month}월 ${day}일`
}