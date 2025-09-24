
// option
const optionList = {
  'LATEST': '최신 순',
  'LIKE' : "좋아요 순",
  'VIEWS' : "조회 순"
}

export const getOptions = () => {
  let arr = [];
  for (const optionListKey in optionList) {
    arr.push(optionListKey);
  }

  return arr;
}


export const options = getOptions();


export const getOptionsName = (option) => {
  return optionList[option];
}

