package com.codingblocks.cbonlineapp.course.checkout

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import com.codingblocks.cbonlineapp.R
import com.codingblocks.cbonlineapp.baseclasses.BaseCBActivity
import com.codingblocks.cbonlineapp.util.extensions.observer
import com.codingblocks.cbonlineapp.util.extensions.replaceFragmentSafely
import com.codingblocks.cbonlineapp.util.extensions.setToolbar
import com.codingblocks.cbonlineapp.util.extensions.showSnackbar
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CheckoutActivity : BaseCBActivity(), PaymentResultWithDataListener {

    private val vm by viewModel<CheckoutViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        setToolbar(checkoutToolbar)
        Checkout.preload(applicationContext)
        vm.getCart()
        replaceFragmentSafely(
            CheckoutOrderDetailsFragment(),
            containerViewId = R.id.checkoutContainer
        )
        vm.paymentStart.observer(this) {
            for (fragment in supportFragmentManager.fragments) {
                supportFragmentManager.beginTransaction().remove(fragment).commitNow()
            }
            payment.apply {
                isVisible = true
                playAnimation()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onPaymentError(code: Int, description: String?, p2: PaymentData?) {
        when (code) {
            Checkout.NETWORK_ERROR -> {
                showOffline()
            }
            Checkout.PAYMENT_CANCELED -> {
                payment.apply {
                    isVisible = false
                    cancelAnimation()
                }
                root.showSnackbar("There was some error please try again")
                replaceFragmentSafely(
                    CheckoutOrderDetailsFragment(),
                    containerViewId = R.id.checkoutContainer
                )
            }
        }
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        // To change body of created functions use File | Settings | File Templates.
        /** Make Network call to capture payment.
         *  Body : {
         *       razorpay_payment_id: payment_ksdnvsdlv,
         *       razorpay_order_id: order_ijkdbsn,
         *       txnId: 192721,
         *       amount: 2394723 (paise)
         *  }
         */
        vm.paymentMap["razorpay_payment_id"] = (p0)!!
        vm.capturePayment {
            GlobalScope.launch {
                delay(3000)
                finish()
            }
        }
    }
}
