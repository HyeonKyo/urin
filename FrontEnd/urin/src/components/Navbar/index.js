import React, { useState, useEffect } from "react";
import { useLocation, Link, useNavigate } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import "./index.css";
import "../../assets/DesignSystem.css";

import {
  Avatar,
  Box,
  Button,
  Checkbox,
  Divider,
  FormControl,
  FormControlLabel,
  IconButton,
  ListItemIcon,
  Menu,
  MenuItem,
  Modal,
  TextField,
  Tooltip,
} from "@mui/material";

import { Logout } from "@mui/icons-material/";
import AccountBalanceIcon from "@mui/icons-material/AccountBalance";
import { passValidation } from "../../store/passValidationSlice";

import logoImg from "../../assets/images/logo_img.png";
import { getMemberId } from "../../store/memberSlice";
import CheckValidation from "../CheckValidation";

const NavComponent = () => {
  const style = {
    position: "absolute",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    minWidth: "360px",
    bgcolor: "background.paper",
    border: "2px solid #000",
    boxShadow: 24,
    borderRadius: "20px",
    padding: "20px 40px 20px 40px",
  };

  const navigate = useNavigate();
  const dispatch = useDispatch();

  const location = useLocation();

  const [token, setToken] = useState(localStorage.getItem("access_token"));
  const [valid, setValid] = useState(false);
  const [disabled, setDisabled] = useState(false);

  const memberName = useSelector((state) => state.member.memberName);
  const memberId = useSelector((state) => state.member.id);

  const [form, setForm] = useState({
    company: "",
    email: "",
  });

  const onChange = (e) => {
    const { name, value } = e.target;

    if (name == "email") {
      const reg =
        /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/;
      setValid(reg.test(e.target.value));
    }
    setForm({
      ...form,
      [name]: value,
    });
  };

  const [anchorEl, setAnchorEl] = useState(null);
  const [openModal, setOpenModal] = useState(false);

  const open = Boolean(anchorEl);

  const handleClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const modalClose = () => setOpenModal(false);

  const passClick = () => {
    setDisabled(false);
    setOpenModal(true);
  };

  const sendValidation = () => {
    if (valid && disabled) {
      dispatch(passValidation({ memberId, navigate }));
      alert("??????????????? ?????????????????????.");
      setOpenModal(false);
    } else if (!valid) {
      alert("????????? ????????? ????????? ???????????????.");
    } else {
      alert("???????????? ????????? ????????? ????????????.");
    }
  };

  useEffect(() => {
    setToken(localStorage.getItem("access_token"));
  }, [token]);

  return (
    <div>
      {!location.pathname.includes("/intro") &&
      !location.pathname.includes("/meeting") &&
      !location.pathname.includes("/report") ? (
        <div className="nav">
          {/* <CheckValidation /> */}

          <input type="checkbox" id="nav-check" />
          <div className="nav-header">
            <a href={`${window.location.protocol}//${window.location.host}`}>
              <img src={logoImg} alt="hello" className="title-logo" />
            </a>
          </div>
          <div className="nav-btn">
            <label htmlFor="nav-check">
              <span></span>
              <span></span>
              <span></span>
            </label>
          </div>
          <div className="nav-right-group">
            <div className="nav-links">
              <a
                className="font-30 font-md"
                href={`${process.env.REACT_APP_PUBLIC_URL}`}
              >
                ????????? ??????
              </a>
              <a
                className="font-30 font-md"
                href={`${process.env.REACT_APP_PUBLIC_URL}mypage`}
              >
                ??? ????????? ??????
              </a>
            </div>

            <div className="avatar">
              <Box
                sx={{
                  display: "flex",
                  alignItems: "center",
                  textAlign: "center",
                }}
              >
                <Tooltip title="?????? ??????">
                  <IconButton
                    onClick={handleClick}
                    size="small"
                    sx={{ ml: 2, mr: 2 }}
                    aria-controls={open ? "account-menu" : undefined}
                    aria-haspopup="true"
                    aria-expanded={open ? "true" : undefined}
                  >
                    <Avatar sx={{ bgcolor: "#0037FA" }}>
                      {memberName ? memberName[0] : "U"}
                    </Avatar>
                  </IconButton>
                </Tooltip>
              </Box>
              <Menu
                anchorEl={anchorEl}
                id="account-menu"
                open={open}
                onClose={handleClose}
                onClick={handleClose}
                PaperProps={{
                  elevation: 0,
                  sx: {
                    overflow: "visible",
                    filter: "drop-shadow(0px 2px 8px rgba(0,0,0,0.32))",
                    mt: 1.5,
                    "& .MuiAvatar-root": {
                      width: 32,
                      height: 32,
                      ml: -0.5,
                      mr: 1,
                    },
                    "&:before": {
                      content: '""',
                      display: "block",
                      position: "absolute",
                      top: 0,
                      right: 14,
                      width: 10,
                      height: 10,
                      bgcolor: "background.paper",
                      transform: "translateY(-50%) rotate(45deg)",
                      zIndex: 0,
                    },
                  },
                }}
                transformOrigin={{ horizontal: "right", vertical: "top" }}
                anchorOrigin={{ horizontal: "right", vertical: "bottom" }}
              >
                <MenuItem>
                  <b className="font-50 font-md">
                    {!memberName ? "Unknown" : memberName}&nbsp;
                  </b>
                  <span className="font-30">??? ???????????????!</span>
                </MenuItem>
                <Divider />
                <MenuItem
                  onClick={passClick}
                  sx={{ "&:hover": { color: "blue" } }}
                >
                  <ListItemIcon>
                    <AccountBalanceIcon fontSize="small" />
                  </ListItemIcon>
                  ????????????
                </MenuItem>
                <Divider />

                <Link to="/logout" className="btn-logout">
                  <MenuItem>
                    <ListItemIcon>
                      <Logout fontSize="small" />
                    </ListItemIcon>
                    ????????????
                  </MenuItem>
                </Link>
              </Menu>
            </div>
          </div>
          <Modal
            open={openModal}
            onClose={modalClose}
            aria-labelledby="modal-modal-title"
            aria-describedby="modal-modal-description"
          >
            <Box component="form" sx={style}>
              <div className="modal-form">
                <span className="font-80 modal-title">?????? ??????</span>
                <span className="font-40 font-md modal-sub-title">
                  ????????? ??????????????????.
                </span>

                <FormControl>
                  <TextField
                    autoFocus
                    required
                    id="company"
                    name="company"
                    type="text"
                    label="?????????"
                    sx={{ fontSize: 40, margin: "5px 0 10px 0" }}
                    onChange={onChange}
                  />
                  <TextField
                    required
                    id="email"
                    name="email"
                    type="text"
                    label="?????? ?????? ??????"
                    error={!valid}
                    sx={{ fontSize: 40, margin: "5px 0 10px 0" }}
                    onChange={onChange}
                  />

                  <div className="modal-checkbox">
                    <FormControlLabel
                      label={
                        <Box component="div" fontSize={12}>
                          ?????? ?????? ????????? ?????? ???????????? ????????? ???????????????.
                        </Box>
                      }
                      control={
                        <Checkbox
                          sx={{
                            "&.Mui-checked": { color: "#0037FA" },
                          }}
                          onChange={() => {
                            setDisabled(!disabled);
                          }}
                        />
                      }
                    />

                    <span className="font-30 font-xs modal-contents"></span>
                  </div>
                </FormControl>
              </div>
              <div className="modal-btn">
                <Button
                  variant="contained"
                  onClick={sendValidation}
                  sx={{
                    backgroundColor: "rgba(0,55,250,1)",
                    margin: "10px",
                    "&:hover": { backgroundColor: "rgba(0,55,250,0.9)" },
                  }}
                >
                  ??????
                </Button>
                <Button
                  variant="contained"
                  onClick={() => setOpenModal(false)}
                  sx={{
                    backgroundColor: "rgba(255,0,0,1)",
                    margin: "10px",
                    "&:hover": { backgroundColor: "rgba(255,0,0,0.7)" },
                  }}
                >
                  ??????
                </Button>
              </div>
            </Box>
          </Modal>
        </div>
      ) : null}
    </div>
  );
};
export default NavComponent;
