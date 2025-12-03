package com.example.wink.data.repository

import com.example.wink.data.model.Answer
import com.example.wink.data.model.Question
import com.example.wink.data.model.Quiz
import javax.inject.Inject

class FakeQuizRepositoryImpl @Inject constructor(): QuizRepository {
    val sampleQuizzes = listOf(
        Quiz(
            id = "rizz_001",
            title = "Tán Tỉnh 101: Cách Mở Lời",
            description = "Kiểm tra kỹ năng 'Icebreaker' của bạn. Ai là người bắt chuyện giỏi nhất?",
            questions = listOf(
                Question(id = "q1_1", text = "Bạn nhắn gì đầu tiên trên app hẹn hò?", correctIndex = 1, answers = listOf(Answer("Hey, em khỏe không?"), Answer("Tôi thấy em thích trekking - em đã leo đỉnh nào đáng nhớ nhất?"), Answer("Em nhìn quen quen."), Answer("Gửi một meme hài hước."))),
                Question(id = "q1_2", text = "Một cô gái từ chối lời mời của bạn. Bạn phản ứng thế nào?", correctIndex = 2, answers = listOf(Answer("Bỏ qua và tìm người khác."), Answer("Hỏi lý do và cố gắng thuyết phục."), Answer("Tuyệt vời, tôi hiểu! Để lần sau vậy. Chúc em ngày vui vẻ."), Answer("Im lặng, không trả lời nữa."))),
                Question(id = "q1_3", text = "Cách khen cô ấy tinh tế nhất?", correctIndex = 0, answers = listOf(Answer("Tôi thích sự thông minh/năng lượng của em hơn là vẻ ngoài."), Answer("Em là người xinh nhất tôi từng gặp."), Answer("Em có biết em rất xinh không?"), Answer("Khen bộ quần áo cô ấy đang mặc."))),
                Question(id = "q1_4", text = "Khi cuộc trò chuyện bị ngắt quãng, bạn nên làm gì?", correctIndex = 3, answers = listOf(Answer("Hỏi: 'Sao em im lặng thế?'"), Answer("Chuyển sang chủ đề hoàn toàn mới."), Answer("Gửi tin nhắn '...'"), Answer("Kể một câu chuyện nhỏ liên quan đến chủ đề trước đó."))),
                Question(id = "q1_5", text = "Cách đặt câu hỏi hay nhất để biết về cô ấy?", correctIndex = 1, answers = listOf(Answer("Em làm nghề gì?"), Answer("Điều gì khiến em hào hứng nhất khi thức dậy mỗi sáng?"), Answer("Em thích màu gì?"), Answer("Em có bao nhiêu người yêu cũ?")))
            )
        ),

        Quiz(
            id = "rizz_002",
            title = "Hẹn Hò Lần Đầu: Tránh Sai Lầm",
            description = "Làm thế nào để buổi hẹn hò đầu tiên thành công mà không bị 'Friend-zone'?",
            questions = listOf(
                Question(id = "q2_1", text = "Bạn nên chọn địa điểm nào cho buổi hẹn đầu?", correctIndex = 3, answers = listOf(Answer("Rạp chiếu phim (quá ít nói)"), Answer("Quán bar ồn ào (không nghe rõ)"), Answer("Nhà hàng sang trọng (quá áp lực)"), Answer("Quán cà phê yên tĩnh hoặc hoạt động nhẹ (để dễ giao tiếp)"),)),
                Question(id = "q2_2", text = "Bạn nên nói về bản thân mình bao nhiêu phần trăm thời gian?", correctIndex = 0, answers = listOf(Answer("Khoảng 30% - 40%"), Answer("Trên 70%"), Answer("Tuyệt đối không nói về mình."), Answer("Chỉ nói khi cô ấy hỏi."))),
                Question(id = "q2_3", text = "Dấu hiệu cô ấy đang 'Bật đèn xanh'?", correctIndex = 1, answers = listOf(Answer("Cô ấy nhìn điện thoại liên tục."), Answer("Cô ấy chạm nhẹ vào tay bạn, hoặc nghiêng người về phía bạn."), Answer("Cô ấy đề nghị chia hóa đơn."), Answer("Cô ấy nói cô ấy độc thân."))),
                Question(id = "q2_4", text = "Ai nên thanh toán hóa đơn?", correctIndex = 3, answers = listOf(Answer("Cô ấy luôn luôn."), Answer("Cả hai nên chia đôi."), Answer("Bạn nên đề nghị trả toàn bộ và không chấp nhận từ chối."), Answer("Bạn nên đề nghị trả, nhưng tôn trọng nếu cô ấy muốn chia một phần."))),
                Question(id = "q2_5", text = "Kết thúc buổi hẹn, bạn nên làm gì?", correctIndex = 2, answers = listOf(Answer("Hôn tạm biệt ngay lập tức."), Answer("Hỏi: 'Khi nào chúng ta gặp lại?'"), Answer("Khen ngợi cô ấy về buổi tối và nói bạn sẽ nhắn tin sau."), Answer("Phân tích lỗi của buổi hẹn.")))
            )
        ),

        Quiz(
            id = "rizz_003",
            title = "Đọc Ngôn Ngữ Cơ Thể",
            description = "Mách bạn cách đọc suy nghĩ cô ấy qua ánh mắt và cử chỉ.",
            questions = listOf(
                Question(id = "q3_1", text = "Khi cô ấy khoanh tay, điều đó thường có nghĩa là gì?", correctIndex = 3, answers = listOf(Answer("Cô ấy lạnh."), Answer("Cô ấy đang buồn ngủ."), Answer("Cô ấy đang chán."), Answer("Cô ấy đang phòng thủ hoặc cảm thấy không thoải mái."))),
                Question(id = "q3_2", text = "Dấu hiệu cô ấy thích bạn qua ánh mắt?", correctIndex = 0, answers = listOf(Answer("Đồng tử nở rộng và cô ấy giao tiếp bằng mắt lâu hơn bình thường."), Answer("Cô ấy nháy mắt liên tục."), Answer("Cô ấy tránh nhìn bạn."), Answer("Cô ấy luôn nhìn chằm chằm vào điện thoại."))),
                Question(id = "q3_3", text = "Hành động nào thể hiện sự tự tin của bạn?", correctIndex = 1, answers = listOf(Answer("Nhìn xuống đất khi nói chuyện."), Answer("Giữ vai thẳng, không vung tay quá nhiều, nói chậm rãi."), Answer("Nói to và cười lớn."), Answer("Đặt tay trong túi quần."))),
                Question(id = "q3_4", text = "Khi cô ấy sao chép (Mirror) cử chỉ của bạn, ý nghĩa là gì?", correctIndex = 2, answers = listOf(Answer("Cô ấy đang chế nhạo bạn."), Answer("Cô ấy bị mắc chứng rối loạn vận động."), Answer("Dấu hiệu của sự gắn kết và đồng cảm."), Answer("Cô ấy đang cố gắng gây sự chú ý."))),
                Question(id = "q3_5", text = "Bạn có nên chạm vào cô ấy trong lần gặp đầu tiên?", correctIndex = 3, answers = listOf(Answer("Tuyệt đối không."), Answer("Chỉ khi cô ấy chạm vào bạn trước."), Answer("Chạm mạnh vào vai cô ấy để gây bất ngờ."), Answer("Chạm nhẹ và tự nhiên (ví dụ: chạm tay khi cười hoặc dẫn qua cửa).")))
            )
        ),

        Quiz(
            id = "rizz_004",
            title = "Xử Lý Tình Huống Khó Xử",
            description = "Những tình huống 'Break-point' trong hẹn hò và cách bạn cứu vãn.",
            questions = listOf(
                Question(id = "q4_1", text = "Cô ấy hỏi về người yêu cũ của bạn. Bạn nên trả lời thế nào?", correctIndex = 1, answers = listOf(Answer("Nói xấu về người cũ để cô ấy thấy bạn tốt."), Answer("Nói ngắn gọn, trung lập, và nhanh chóng chuyển chủ đề sang cô ấy."), Answer("Kể chi tiết về mọi mối quan hệ."), Answer("Nói rằng bạn chưa từng có người yêu cũ."))),
                Question(id = "q4_2", text = "Bạn vô tình quên tên cô ấy. Bạn sẽ làm gì?", correctIndex = 3, answers = listOf(Answer("Cứ tiếp tục nói chuyện như không có gì."), Answer("Hỏi cô ấy tên cô ấy là gì."), Answer("Nói dối rằng bạn đang có vấn đề trí nhớ."), Answer("Thú nhận một cách hóm hỉnh, ví dụ: 'Tuyệt vời, tên tôi là X, nhưng tên thiên thần của tôi là gì?'"))),
                Question(id = "q4_3", text = "Khi có khoảng lặng không mong muốn, bạn nên làm gì?", correctIndex = 0, answers = listOf(Answer("Bình tĩnh, mỉm cười và đưa ra một quan sát nhỏ về môi trường xung quanh."), Answer("Bắt đầu hát một bài."), Answer("Kéo điện thoại ra lướt Facebook."), Answer("Đứng dậy và nói lời tạm biệt."))),
                Question(id = "q4_4", text = "Cô ấy có vẻ buồn vì một chuyện nào đó. Cách tốt nhất để an ủi là gì?", correctIndex = 2, answers = listOf(Answer("Đưa ra giải pháp ngay lập tức."), Answer("Kể về vấn đề của bạn để cô ấy thấy không tệ bằng."), Answer("Lắng nghe một cách đồng cảm, hỏi cảm xúc của cô ấy và hỏi cô ấy muốn gì."), Answer("Nói: 'Đừng lo, mọi chuyện rồi sẽ ổn thôi.'"))),
                Question(id = "q4_5", text = "Bạn nên đề cập đến các chủ đề chính trị/tôn giáo khi nào?", correctIndex = 3, answers = listOf(Answer("Ngay từ tin nhắn đầu tiên."), Answer("Trong buổi hẹn đầu tiên."), Answer("Không bao giờ."), Answer("Sau khi đã có sự kết nối sâu sắc và cả hai đều thoải mái.")))
            )
        ),

        Quiz(id = "rizz_005", title = "Nghệ Thuật Trêu Đùa (Teasing)", description = "Sử dụng sự hài hước để xây dựng kết nối.", questions = listOf(
            Question(id = "q5_1", text = "Cách trêu cô ấy về sở thích của cô ấy?", correctIndex = 0, answers = listOf(Answer("Đưa ra lời trêu chọc nhẹ nhàng, không gây tổn thương, và kèm theo nụ cười."), Answer("Nói thẳng rằng sở thích của cô ấy thật trẻ con."), Answer("Phớt lờ sở thích đó."), Answer("Dùng những từ ngữ xúc phạm."))),
            Question(id = "q5_2", text = "Ranh giới của việc trêu đùa là gì?", correctIndex = 3, answers = listOf(Answer("Bất cứ điều gì cũng được miễn là bạn cười."), Answer("Chỉ trêu khi có người khác ở đó."), Answer("Trêu đùa về những khuyết điểm cá nhân."), Answer("Không bao giờ trêu về ngoại hình, gia đình, hoặc các vấn đề nhạy cảm."))),
            Question(id = "q5_3", text = "Khi cô ấy trêu lại bạn, bạn nên làm gì?", correctIndex = 1, answers = listOf(Answer("Nghiêm túc và nói cô ấy dừng lại."), Answer("Cười, chấp nhận nó và có thể trêu lại một cách tinh tế hơn."), Answer("Bỏ về."), Answer("Giả vờ không nghe thấy."))),
            Question(id = "q5_4", text = "Tại sao trêu đùa lại hiệu quả trong tán tỉnh?", correctIndex = 2, answers = listOf(Answer("Nó làm cô ấy cảm thấy thấp kém hơn."), Answer("Nó giúp bạn chứng tỏ sự vượt trội."), Answer("Nó tạo ra sự rung cảm, vui vẻ, và phá vỡ sự căng thẳng (sexual tension)."), Answer("Nó là cách duy nhất để cô ấy chú ý."))),
            Question(id = "q5_5", text = "Trêu đùa nên chiếm bao nhiêu phần trăm cuộc trò chuyện?", correctIndex = 0, answers = listOf(Answer("Khoảng 20-30%, cân bằng với các câu hỏi ý nghĩa."), Answer("100%, nói toàn chuyện đùa."), Answer("Không nên trêu đùa, phải thật nghiêm túc."), Answer("Chỉ nên trêu khi cô ấy có tâm trạng tốt.")))
        )),

        Quiz(id = "rizz_006", title = "Xây Dựng Sự Tin Tưởng", description = "Các bước để cô ấy tin tưởng và mở lòng với bạn.", questions = listOf(
            Question(id = "q6_1", text = "Cách tốt nhất để thể hiện sự chân thành?", correctIndex = 1, answers = listOf(Answer("Khoe khoang về tài sản."), Answer("Chia sẻ những điểm yếu/vulnerability nhỏ của bạn."), Answer("Nói rằng bạn yêu cô ấy ngay lập tức."), Answer("Tặng cô ấy những món quà đắt tiền."))),
            Question(id = "q6_2", text = "Khi cô ấy chia sẻ bí mật, bạn nên làm gì?", correctIndex = 3, answers = listOf(Answer("Chia sẻ bí mật đó với bạn bè."), Answer("Dùng nó để trêu chọc cô ấy."), Answer("Đánh giá và đưa ra lời khuyên không được hỏi."), Answer("Lắng nghe mà không phán xét và giữ kín."))),
            Question(id = "q6_3", text = "Sự nhất quán trong hành động và lời nói gọi là gì?", correctIndex = 0, answers = listOf(Answer("Integrity (Tính chính trực)"), Answer("Charisma (Sự lôi cuốn)"), Answer("Compliance (Tuân thủ)"), Answer("Superiority (Sự vượt trội)"))),
            Question(id = "q6_4", text = "Điều gì phá hủy niềm tin nhanh nhất?", correctIndex = 2, answers = listOf(Answer("Trễ hẹn 5 phút."), Answer("Quên đặt món cô ấy thích."), Answer("Nói dối về những điều nhỏ nhặt hoặc hứa nhưng không làm."), Answer("Mặc áo sơ mi xấu.")),),
            Question(id = "q6_5", text = "Bạn nên làm gì nếu bạn sai?", correctIndex = 1, answers = listOf(Answer("Đổ lỗi cho cô ấy."), Answer("Thành thật xin lỗi và sửa chữa nếu có thể."), Answer("Phớt lờ lỗi đó."), Answer("Giả vờ là chuyện đùa.")))
        )),

        Quiz(id = "rizz_007", title = "Tán Gái qua Tin Nhắn", description = "Kỹ năng nhắn tin đỉnh cao, giữ lửa cuộc trò chuyện.", questions = listOf(
            Question(id = "q7_1", text = "Thời gian lý tưởng để trả lời tin nhắn của cô ấy?", correctIndex = 2, answers = listOf(Answer("Trong vòng 1 giây."), Answer("Chờ 24 giờ để tạo 'mystique'."), Answer("Trong vòng 1-3 giờ, không quá nhanh cũng không quá chậm."), Answer("Chỉ trả lời sau khi cô ấy nhắn tin hai lần."))),
            Question(id = "q7_2", text = "Độ dài tin nhắn của bạn nên là bao nhiêu?", correctIndex = 0, answers = listOf(Answer("Tương đương hoặc ngắn hơn một chút so với tin nhắn của cô ấy."), Answer("Luôn luôn viết một đoạn văn dài."), Answer("Chỉ dùng một từ/emoji."), Answer("Dùng voice note cho mọi tin nhắn."))),
            Question(id = "q7_3", text = "Mục đích cuối cùng của việc nhắn tin là gì?", correctIndex = 1, answers = listOf(Answer("Trao đổi thông tin về cuộc sống cá nhân."), Answer("Thiết lập buổi hẹn hò trực tiếp."), Answer("Kể truyện cười."), Answer("Thảo luận về chính trị thế giới."))),
            Question(id = "q7_4", text = "Khi nào nên dùng biểu tượng cảm xúc (Emoji)?", correctIndex = 3, answers = listOf(Answer("Trong mọi tin nhắn."), Answer("Không bao giờ, quá trẻ con."), Answer("Chỉ dùng khi tức giận."), Answer("Dùng để thêm ngữ điệu và làm cho tin nhắn nhẹ nhàng hơn."))),
            Question(id = "q7_5", text = "Nội dung tin nhắn nào nên tránh?", correctIndex = 2, answers = listOf(Answer("Chia sẻ về ngày làm việc."), Answer("Hỏi về kế hoạch cuối tuần."), Answer("Nhắn tin quá khích, đòi hỏi câu trả lời ngay lập tức, hoặc chỉ nói về tình dục."), Answer("Gửi ảnh meme chất lượng cao.")))
        )),

        Quiz(id = "rizz_008", title = "Phát Triển Mối Quan Hệ", description = "Từ hẹn hò đến mối quan hệ nghiêm túc, bạn cần làm gì.", questions = listOf(
            Question(id = "q8_1", text = "Cách đặt ra 'Definitive talk' (Nói rõ mối quan hệ) hiệu quả nhất?", correctIndex = 0, answers = listOf(Answer("Khi đã có sự ổn định, hỏi rõ ràng về mong muốn của cả hai."), Answer("Trong bữa tiệc ồn ào."), Answer("Gửi tin nhắn 'Chúng ta là gì của nhau?'"), Answer("Sau khi cãi nhau."))),
            Question(id = "q8_2", text = "Cách duy trì 'Spark' (Lửa tình) lâu dài?", correctIndex = 3, answers = listOf(Answer("Làm mọi thứ giống nhau mỗi ngày."), Answer("Không bao giờ cãi nhau."), Answer("Tặng quà đắt tiền thường xuyên."), Answer("Duy trì sự độc lập, bất ngờ và chất lượng thời gian dành cho nhau."))),
            Question(id = "q8_3", text = "Trong một cuộc tranh cãi, ưu tiên của bạn là gì?", correctIndex = 1, answers = listOf(Answer("Phải thắng cuộc tranh luận."), Answer("Hiểu quan điểm của đối phương và tìm giải pháp chung."), Answer("Im lặng và bỏ đi."), Answer("Đổ lỗi cho mọi thứ."))),
            Question(id = "q8_4", text = "Khi nào nên giới thiệu cô ấy với gia đình?", correctIndex = 2, answers = listOf(Answer("Sau buổi hẹn đầu tiên."), Answer("Chỉ khi cô ấy yêu cầu."), Answer("Khi mối quan hệ đã ổn định, rõ ràng và có sự đồng ý của cả hai."), Answer("Chỉ khi bạn cần người nấu ăn cho bố mẹ."))),
            Question(id = "q8_5", text = "Điều gì quan trọng nhất trong một mối quan hệ lành mạnh?", correctIndex = 0, answers = listOf(Answer("Giao tiếp cởi mở, tôn trọng và tin tưởng."), Answer("Tiền bạc."), Answer("Thời gian ở bên nhau 24/7."), Answer("Sở thích chung.")))
        )),

        Quiz(id = "rizz_009", title = "Lôi Cuốn và Độc Lập", description = "Sự tự tin và giá trị cá nhân thu hút cô gái.", questions = listOf(
            Question(id = "q9_1", text = "Sự lôi cuốn (Charisma) đến từ đâu?", correctIndex = 3, answers = listOf(Answer("Mặc quần áo hàng hiệu."), Answer("Nói về những thành tích lớn."), Answer("Cố gắng làm hài lòng tất cả mọi người."), Answer("Có mục đích sống rõ ràng và theo đuổi nó một cách đam mê."))),
            Question(id = "q9_2", text = "Bạn nên làm gì khi cô ấy hủy hẹn vào phút cuối?", correctIndex = 0, answers = listOf(Answer("Nói rằng không sao, nhưng không hủy bỏ kế hoạch của riêng bạn (ví dụ: đi ăn một mình/với bạn)."), Answer("Tỏ ra giận dữ và hủy mọi cuộc hẹn trong tương lai."), Answer("Hỏi cô ấy phải làm gì để cô ấy chấp nhận hẹn."), Answer("Ngay lập tức đề nghị gặp lại sau 10 phút."))),
            Question(id = "q9_3", text = "Thế nào là 'High-Value Man'?", correctIndex = 1, answers = listOf(Answer("Người có thu nhập cao nhất."), Answer("Người có giá trị cá nhân, tôn trọng bản thân và đối xử tốt với người khác."), Answer("Người hay nói to và chỉ huy."), Answer("Người không bao giờ nghe lời."))),
            Question(id = "q9_4", text = "Bạn nên có bao nhiêu sở thích/mục tiêu ngoài cô ấy?", correctIndex = 2, answers = listOf(Answer("Không có gì, cô ấy là tất cả."), Answer("Một sở thích chung với cô ấy."), Answer("Nhiều sở thích cá nhân để làm phong phú cuộc sống của bạn."), Answer("Chỉ xem phim và chơi game."))),
            Question(id = "q9_5", text = "Điều gì chứng tỏ bạn là người độc lập?", correctIndex = 3, answers = listOf(Answer("Không bao giờ nghe điện thoại."), Answer("Luôn luôn làm theo ý mình."), Answer("Không bao giờ cần lời khuyên của cô ấy."), Answer("Không đặt cô ấy làm trung tâm duy nhất của hạnh phúc và có cuộc sống riêng.")))
        )),

        Quiz(id = "rizz_010", title = "Sự Hài Hước Tinh Tế", description = "Cách dùng hài hước để cưa đổ nàng.", questions = listOf(
            Question(id = "q10_1", text = "Kiểu hài hước nào nên tránh tuyệt đối?", correctIndex = 0, answers = listOf(Answer("Hài hước gây tổn thương, chỉ trích hoặc mang tính xúc phạm."), Answer("Hài hước tự trào (Self-deprecating)."), Answer("Những câu chuyện cười ngớ ngẩn."), Answer("Hài hước liên quan đến ẩm thực."))),
            Question(id = "q10_2", text = "Phản ứng tốt nhất khi cô ấy cười?", correctIndex = 1, answers = listOf(Answer("Hỏi: 'Sao em cười?'"), Answer("Cười cùng cô ấy và tiếp tục mạch chuyện."), Answer("Lặp lại câu chuyện cười đó 10 lần."), Answer("Tỏ ra nghiêm túc."))),
            Question(id = "q10_3", text = "Hài hước tự trào (Tự cười mình) có tốt không?", correctIndex = 3, answers = listOf(Answer("Tuyệt đối không, nó làm bạn trông yếu đuối."), Answer("Chỉ nên dùng khi cô ấy buồn."), Answer("Nó thể hiện bạn không có tự trọng."), Answer("Nó thể hiện sự tự tin và khả năng không quá nghiêm trọng hóa bản thân."))),
            Question(id = "q10_4", text = "Một câu nói hóm hỉnh bạn có thể dùng khi trễ hẹn (rất hiếm khi)?", correctIndex = 2, answers = listOf(Answer("Tôi không có lỗi."), Answer("Tôi bị kẹt xe."), Answer("Tôi xin lỗi, tôi đã đến muộn vì đang giải cứu một chú chó con (nói một cách hóm hỉnh và xin lỗi chân thành sau đó)."), Answer("Em phải đợi tôi."))),
            Question(id = "q10_5", text = "Khi kể một câu chuyện cười, bạn nên làm gì?", correctIndex = 0, answers = listOf(Answer("Giữ thái độ tự tin, kể dứt khoát và không giải thích quá nhiều."), Answer("Hỏi cô ấy xem cô ấy có hiểu không."), Answer("Kể quá nhanh và lắp bắp."), Answer("Cười trước khi kể xong.")))
        ))
    )
    override suspend fun getAllQuizzes(): List<Quiz> = sampleQuizzes
    override suspend fun getQuizById(id: String): Quiz? = sampleQuizzes.firstOrNull { it.id == id }
}