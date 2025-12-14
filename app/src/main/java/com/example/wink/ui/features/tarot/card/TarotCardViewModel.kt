package com.example.wink.ui.features.tarot.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.R
import com.example.wink.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val CARD_RETRY_COST = 50

@HiltViewModel
class TarotCardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TarotCardState())
    val uiState: StateFlow<TarotCardState> = _uiState.asStateFlow()

    private val tarotDeck: List<TarotCardInfo> = listOf(
        TarotCardInfo(
            id = 0,
            name = "The Fool",
            shortMeaning = "Khởi đầu tự do, bất ngờ.",
            detail = "Hãy mở lòng đón nhận một cuộc phiêu lưu tình cảm không toan tính. "
                    + "Cứ tin tưởng và tận hưởng niềm vui hiện tại thay vì lo lắng quá nhiều về tương lai.",
            imageRes = R.drawable.___the_fool
        ),
        TarotCardInfo(
            id = 1,
            name = "The Magician",
            shortMeaning = "Bạn nắm quyền chủ động.",
            detail = "Bạn có đủ sức hút và kỹ năng để chinh phục đối phương. Đừng chờ đợi, đây là lúc để " +
                    "biến những mong muốn thành hiện thực bằng hành động cụ thể.",
            imageRes = R.drawable.___the_magician
        ),
        TarotCardInfo(
            id = 2,
            name = "The High Priestess",
            shortMeaning = "Sự bí ẩn đầy quyến rũ.",
            detail = "Có những cảm xúc chưa được nói ra. Hãy tin vào trực giác của bạn về người ấy. " +
                    "Một chút bí ẩn sẽ khiến bạn trở nên khao khát hơn trong mắt đối phương.",
            imageRes = R.drawable.___the_priestess
        ),
        TarotCardInfo(
            id = 3,
            name = "The Empress",
            shortMeaning = "Tình yêu nồng nàn, trù phú.",
            detail = "Một giai đoạn ngập tràn sự quan tâm, chăm sóc và lãng mạn. " +
                    "Sức hút của bạn đến từ sự dịu dàng và vẻ đẹp tự nhiên đầy nữ tính.",
            imageRes = R.drawable.___the_empress
        ),
        TarotCardInfo(
            id = 4,
            name = "The Emperor",
            shortMeaning = "Mối quan hệ nghiêm túc, ổn định.",
            detail = "Tình yêu cần sự rõ ràng và cam kết vững chắc. Có thể đối phương là người " +
                    "mạnh mẽ, thích che chở nhưng hơi gia trưởng hoặc khô khan.",
            imageRes = R.drawable.___the_emperor
        ),
        TarotCardInfo(
            id = 5,
            name = "The Hierophant",
            shortMeaning = "Cam kết lâu dài, truyền thống.",
            detail = "Mối quan hệ hướng tới sự chấp thuận của xã hội hoặc hôn nhân. " +
                    "Hai bạn đang tìm kiếm sự đồng điệu về tâm hồn và giá trị sống hơn là sự phá cách.",
            imageRes = R.drawable.___the_hierophant
        ),
        TarotCardInfo(
            id = 6,
            name = "The Lovers",
            shortMeaning = "Sự lựa chọn của trái tim.",
            detail = "Một sự kết nối sâu sắc và mãnh liệt đang diễn ra. " +
                    "Tuy nhiên, bạn có thể phải đứng trước một quyết định quan trọng liên quan đến tình cảm này.",
            imageRes = R.drawable.___the_lovers
        ),
        TarotCardInfo(
            id = 7,
            name = "The Chariot",
            shortMeaning = "Quyết tâm chinh phục.",
            detail = "Đừng để rào cản ngăn bước bạn. Hãy kiểm soát cảm xúc và tiến tới mục tiêu. " +
                    "Sự tự tin chính là vũ khí mạnh nhất của bạn lúc này.",
            imageRes = R.drawable.___the_chariot
        ),
        TarotCardInfo(
            id = 8,
            name = "Justice",
            shortMeaning = "Công bằng và rõ ràng.",
            detail = "Bạn sẽ nhận được chính xác những gì bạn đã bỏ ra. " +
                    "Nếu bạn chân thành, tình yêu sẽ đến. Nếu mối quan hệ mất cân bằng, đã đến lúc đàm phán lại.",
            imageRes = R.drawable.___justice
        ),
        TarotCardInfo(
            id = 9,
            name = "The Hermit",
            shortMeaning = "Lắng nghe tiếng nói bên trong.",
            detail = "Tạm thời tách biệt để hiểu rõ trái tim mình muốn gì. " +
                    "Đừng vội vàng tìm kiếm người mới khi bạn chưa thực sự hiểu bản thân.",
            imageRes = R.drawable.___the_hermit
        ),
        TarotCardInfo(
            id = 10,
            name = "Wheel of Fortune",
            shortMeaning = "Định mệnh sắp đặt.",
            detail = "Một cuộc gặp gỡ tình cờ hoặc sự thay đổi bất ngờ đang đến. " +
                    "Hãy để mọi thứ diễn ra tự nhiên, vũ trụ đang xoay chuyển tình thế cho bạn.",
            imageRes = R.drawable.___wheel_of_fortune
        ),
        TarotCardInfo(
            id = 11,
            name = "Strength",
            shortMeaning = "Sự kiên nhẫn dịu dàng.",
            detail = "Không cần gồng mình, sự thấu hiểu và mềm mỏng mới là cách để " +
                    "thu phục trái tim đối phương và vượt qua những thử thách hiện tại.",
            imageRes = R.drawable.___strength
        ),
        TarotCardInfo(
            id = 12,
            name = "The Hanged Man",
            shortMeaning = "Chậm lại để nhìn nhận.",
            detail = "Mọi thứ dường như đang chững lại. Đừng cố ép buộc. " +
                    "Hãy thay đổi góc nhìn hoặc hy sinh một chút cái tôi để hiểu đối phương hơn.",
            imageRes = R.drawable.___the_hanged_man
        ),
        TarotCardInfo(
            id = 13,
            name = "Death",
            shortMeaning = "Kết thúc để bắt đầu mới.",
            detail = "Một giai đoạn hoặc một mối quan hệ cũ cần phải khép lại triệt để. " +
                    "Sự lột xác này là cần thiết để đón nhận một tình yêu tốt đẹp hơn đang tới.",
            imageRes = R.drawable.___death
        ),
        TarotCardInfo(
            id = 14,
            name = "Temperance",
            shortMeaning = "Hòa hợp và cân bằng.",
            detail = "Tình yêu cần sự kiên nhẫn và vun đắp từ từ. " +
                    "Hai bạn đang học cách dung hòa những khác biệt để tạo nên một mối liên kết bền vững.",
            imageRes = R.drawable.___temperance
        ),
        TarotCardInfo(
            id = 15,
            name = "The Devil",
            shortMeaning = "Cám dỗ và đam mê.",
            detail = "Một sức hút thể xác mãnh liệt nhưng coi chừng sự ràng buộc độc hại. " +
                    "Đừng để sự ám ảnh hay ghen tuông kiểm soát lý trí của bạn.",
            imageRes = R.drawable.___the_devil
        ),
        TarotCardInfo(
            id = 16,
            name = "The Star",
            shortMeaning = "Hy vọng và chữa lành.",
            detail = "Sau những tổn thương, trái tim bạn đang được chữa lành. " +
                    "Hãy mở lòng ra, một tương lai tươi sáng và một người xứng đáng đang chờ bạn phía trước.",
            imageRes = R.drawable.___the_star
        ),
        TarotCardInfo(
            id = 17,
            name = "The Tower",
            shortMeaning = "Sự rung chuyển bất ngờ.",
            detail = "Một sự kiện bất ngờ xảy ra làm thay đổi nền tảng mối quan hệ. " +
                    "Dù đau đớn, nhưng sự sụp đổ này là cần thiết để loại bỏ những gì không còn chân thật.",
            imageRes = R.drawable.___the_tower
        ),
        TarotCardInfo(
            id = 18,
            name = "The Moon",
            shortMeaning = "Mơ hồ và bối rối.",
            detail = "Có những điều không rõ ràng hoặc những nỗi sợ vô hình đang bao trùm. " +
                    "Cẩn thận với sự hiểu lầm và đừng để trí tưởng tượng đi quá xa thực tế.",
            imageRes = R.drawable.___the_moon
        ),
        TarotCardInfo(
            id = 19,
            name = "The Sun",
            shortMeaning = "Niềm vui rạng rỡ.",
            detail = "Hạnh phúc viên mãn và sự rõ ràng. Tình yêu của bạn đang trong giai đoạn " +
                    "ấm áp nhất, mọi khúc mắc đều được phơi bày và giải quyết êm đẹp.",
            imageRes = R.drawable.___the_sun
        ),
        TarotCardInfo(
            id = 20,
            name = "Judgement",
            shortMeaning = "Thời khắc thức tỉnh.",
            detail = "Đã đến lúc đưa ra quyết định quan trọng hoặc tha thứ cho quá khứ. " +
                    "Sự hồi sinh trong tình cảm giúp bạn nhận ra ai mới là người thực sự quan trọng.",
            imageRes = R.drawable.___judgement
        ),
        TarotCardInfo(
            id = 21,
            name = "The World",
            shortMeaning = "Hạnh phúc trọn vẹn.",
            detail = "Một cái kết có hậu hoặc một chương mới tuyệt vời. " +
                    "Bạn cảm thấy trọn vẹn và hòa hợp hoàn hảo với người mình yêu.",
            imageRes = R.drawable.___the_world
        )
    )

    /** Rút (hoặc rút lại) lá bài.
     *  - Nếu chưa có currentCard -> rút miễn phí
     *  - Nếu đã có rồi -> show dialog hỏi dùng 50 Rizz (onDrawAgainClicked())
     */
    fun drawCardInternal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            delay(600)

            val card = tarotDeck.random()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentCard = card,
                    error = null
                )
            }
        }
    }

    /** Được gọi từ UI khi bấm nút "Rút bài" / "Rút lại" */
    fun onDrawButtonClicked() {
        val s = _uiState.value
        if (s.currentCard == null) {
            // Lần đầu -> miễn phí
            drawCardInternal()
        } else {
            // Đã có lá bài -> hỏi dùng 50 Rizz
            _uiState.update { it.copy(showConfirmDialog = true) }
        }
    }

    fun onDismissDialogs() {
        _uiState.update {
            it.copy(
                showConfirmDialog = false,
                showNotEnoughDialog = false
            )
        }
    }

    /** User bấm "Chốt đơn" -> trừ 50 Rizz, nếu thành công thì rút lại bài */
    fun onConfirmUseRizz() {
        viewModelScope.launch {
            val success = userRepository.spendRizz(CARD_RETRY_COST)
            if (success) {
                _uiState.update { it.copy(showConfirmDialog = false) }
                drawCardInternal()
            } else {
                _uiState.update {
                    it.copy(
                        showConfirmDialog = false,
                        showNotEnoughDialog = true
                    )
                }
            }
        }
    }

    /** Khi bấm nút trong dialog "Không đủ Rizz" */
    fun onNotEnoughDialogHandled() {
        _uiState.update { it.copy(showNotEnoughDialog = false) }
    }
}
