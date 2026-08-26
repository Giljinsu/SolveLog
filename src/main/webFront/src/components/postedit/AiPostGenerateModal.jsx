import './AiPostGenerateModal.css';
import {useState} from 'react';
import {Button2} from '../common/Button.jsx';
import axios from '../../context/axiosInstance.js';

// AI 로 문제풀이 Markdown 초안 생성하는 모달
const AiPostGenerateModal = ({onClose, onGenerated, createAlarm}) => {
  const [problemContent, setProblemContent] = useState('');
  const [solutionCode, setSolutionCode] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);

  const onClickGenerate = async () => {
    if (isGenerating) return; // 중복 요청 방지

    if (!problemContent.trim() || !solutionCode.trim()) {
      createAlarm('문제 설명과 풀이 코드를 모두 입력해주세요.', 'bad');
      return;
    }

    setIsGenerating(true);
    try {
      const res = await axios.post('/api/ai/generatePostDraft', {
        problemContent,
        solutionCode,
      });

      onGenerated(res.data.markdown);
    } catch (e) {
      const message = e.response?.data?.message
          || 'AI 초안 생성에 실패했습니다. 잠시 후 다시 시도해주세요.';
      createAlarm(message, 'bad');
    } finally {
      setIsGenerating(false);
      onClose();
    }
  };

  const onBackdropClick = () => {
    if (isGenerating) return; // 생성 중에는 닫기 방지
    onClose();
  };

  return (
      <div className="ai-modal-backdrop" onClick={onBackdropClick}>
        <div className="ai-modal-content" onClick={(e) => e.stopPropagation()}>
          <h2>AI로 문제풀이 작성</h2>

          <div className="ai-modal-field">
            <label>문제 설명 *</label>
            <textarea
                placeholder="문제 내용을 입력해주세요."
                value={problemContent}
                onChange={(e) => setProblemContent(e.target.value)}
                disabled={isGenerating}
            />
          </div>

          <div className="ai-modal-field">
            <label>풀이 코드 *</label>
            <textarea
                className="ai-modal-code"
                placeholder="작성한 풀이 코드를 입력해주세요."
                value={solutionCode}
                onChange={(e) => setSolutionCode(e.target.value)}
                disabled={isGenerating}
            />
          </div>

          <div className="ai-modal-footer">
            <Button2
                buttonText={'취소'}
                buttonType={'button'}
                buttonEvent={onBackdropClick}
                disabled={isGenerating}
            />
            <Button2
                buttonText={isGenerating ? '생성 중...' : '생성하기'}
                buttonType={'button'}
                buttonEvent={onClickGenerate}
                disabled={isGenerating}
            />
          </div>
        </div>
      </div>
  );
};

export default AiPostGenerateModal;