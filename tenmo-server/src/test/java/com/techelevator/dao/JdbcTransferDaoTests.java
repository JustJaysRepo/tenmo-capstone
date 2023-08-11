package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.exception.AccountNotFoundException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferStatus;
import com.techelevator.tenmo.model.TransferType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class JdbcTransferDaoTests extends BaseDaoTests {

    protected static final Transfer TRANSFER_1 = new Transfer(3001, TransferType.REQUEST, TransferStatus.PENDING, 2001, 2002, new BigDecimal("100.00"));
    protected static final Transfer TRANSFER_2 = new Transfer(3002, TransferType.SEND, TransferStatus.APPROVED, 2002, 2001, new BigDecimal("200.00"));
    protected static final Transfer TRANSFER_3 = new Transfer(3003, TransferType.REQUEST, TransferStatus.REJECTED, 2003, 2001, new BigDecimal("300.00"));

    private JdbcTransferDao sut;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcTransferDao(jdbcTemplate);
    }


    @Test(expected = EmptyResultDataAccessException.class)
    public void getTransferById_given_invalid_id_throws_exception() {
        sut.getTransferById(0);
    }

    @Test
    public void getTransferById_given_valid_id_returns_transfer() {
        Transfer actualTransfer = sut.getTransferById(TRANSFER_1.getTransfer_id());
       assertTransferEquals(TRANSFER_1, actualTransfer);
    }

    @Test
    public void getAllTransfer_returns_all_transfers() {
        List<Transfer> actualTransfers = sut.findAll();
        actualTransfers.sort(Comparator.comparing(Transfer::getTransfer_id));
        assertTransferEquals(TRANSFER_1, actualTransfers.get(0));
        assertTransferEquals(TRANSFER_2, actualTransfers.get(1));
        assertTransferEquals(TRANSFER_3, actualTransfers.get(2));
    }


    // test get transfers by user ID
    @Test
    public void getTransferByUserId_given_invalid_id_return_empty_list(){
        List<Transfer> actualTransfers = sut.getTransfersByUserId(0);
        assertEquals(0, actualTransfers.size());
    };

    @Test
    public void getTransferByUserId_given_valid_id_return_list(){
        List<Transfer> actualTransfers = sut.getTransfersByUserId(1001);
        actualTransfers.sort(Comparator.comparing(Transfer::getTransfer_id));
        assertTransferEquals(TRANSFER_1, actualTransfers.get(0));
        assertTransferEquals(TRANSFER_2, actualTransfers.get(1));
        assertTransferEquals(TRANSFER_3, actualTransfers.get(2));
    };


    @Test(expected = AccountNotFoundException.class)
    public void createTransfer_invalidAccountFrom_throwsException() {
        Transfer transfer = new Transfer(0, TransferType.SEND, TransferStatus.APPROVED, 9999, 2002, new BigDecimal("500.00"));
        sut.create(transfer);
    }

    @Test(expected = AccountNotFoundException.class)
    public void createTransfer_invalidAccountTo_throwsException() {
        Transfer transfer = new Transfer(0, TransferType.SEND, TransferStatus.APPROVED, 2001, 9999, new BigDecimal("500.00"));
        sut.create(transfer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTransfer_sameAccountFromAndTo_throwsException() {
        Transfer transfer = new Transfer(0, TransferType.SEND, TransferStatus.APPROVED, 2001, 2001, new BigDecimal("500.00"));
        sut.create(transfer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTransfer_invalidTransferType_throwsException() {
        Transfer transfer = new Transfer(0, null, TransferStatus.APPROVED, 2001, 2002, new BigDecimal("500.00"));
        sut.create(transfer);
    }

    @Test
    public void createTransfer_given_transfer_type__send_update_status_() {
        Transfer transfer = new Transfer(0, TransferType.SEND, null, 2001, 2002, new BigDecimal("500.00"));
        Transfer createdTransfer = sut.create(transfer);
        assertNotNull(createdTransfer);
        assertEquals(TransferStatus.APPROVED, createdTransfer.getTransfer_status());
    }
      @Test
    public void createTransfer_given_transfer_type__request_update_status_() {
        Transfer transfer = new Transfer(0, TransferType.REQUEST, null, 2001, 2002, new BigDecimal("500.00"));
        Transfer createdTransfer = sut.create(transfer);
        assertNotNull(createdTransfer);
        assertEquals(TransferStatus.PENDING, createdTransfer.getTransfer_status());
    }

    @Test
    public void createTransfer_validTransfer_returnsCreatedTransfer() {
        Transfer transfer = new Transfer(0, TransferType.SEND, TransferStatus.APPROVED, 2001, 2002, new BigDecimal("500.00"));
        Transfer createdTransfer = sut.create(transfer);
        assertNotNull(createdTransfer);
        assertNotEquals(0, createdTransfer.getTransfer_id());
        assertTransferEquals(transfer, createdTransfer);
    }

    @Test
    public void updateTransfer_validTransfer_updatesTransferSuccessfully() {
        Transfer existingTransfer = sut.getTransferById(TRANSFER_1.getTransfer_id());
        Transfer updatedTransfer = new Transfer(existingTransfer.getTransfer_id(), TransferType.SEND, TransferStatus.APPROVED, existingTransfer.getAccount_from(), existingTransfer.getAccount_to(), new BigDecimal("600.00"));
        assertTrue(sut.update(existingTransfer.getTransfer_id(), updatedTransfer));
        Transfer retrievedTransfer = sut.getTransferById(existingTransfer.getTransfer_id());
        assertTransferEquals(updatedTransfer, retrievedTransfer);
    }


    private  void assertTransferEquals(Transfer expected, Transfer actual) {
        assertEquals(expected.getTransfer_id(), actual.getTransfer_id());
        assertEquals(expected.getTransfer_type(), actual.getTransfer_type());
        assertEquals(expected.getTransfer_status(), actual.getTransfer_status());
        assertEquals(expected.getAccount_from(), actual.getAccount_from());
        assertEquals(expected.getAccount_to(), actual.getAccount_to());
        assertEquals(expected.getAmount(), actual.getAmount());
    }

}

