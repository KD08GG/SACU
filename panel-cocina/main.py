"""
╔══════════════════════════════════════════════════════╗
║      CAFETERIA ORDER MANAGEMENT SYSTEM               ║
║      Python + CustomTkinter                          ║
║      Translated from React/TypeScript prototype      ║
╚══════════════════════════════════════════════════════╝

HOW TO RUN:
  pip install customtkinter
  python main.py

DATABASE CONNECTION:
  Search for "── DATABASE HOOK ──" comments throughout
  this file to find every place where Firebase (or any
  other backend) should be connected. Each hook is
  clearly marked with what data to read/write.
"""

import customtkinter as ctk
from tkinter import messagebox
import tkinter as tk
from datetime import datetime, date
from dataclasses import dataclass
from typing import Optional, Callable
import threading
import time


# ════════════════════════════════════════════════════════
#  DATA MODELS  (mirrors types.ts)
# ════════════════════════════════════════════════════════

@dataclass
class OrderItem:
    name: str
    quantity: int
    notes: str = ""


@dataclass
class Order:
    id: int
    order_number: str
    user_id: str
    user_name: str
    payment_type: str          # 'credit' | 'debit' | 'cash' | 'meal-plan'
    items: list
    timestamp: datetime
    status: str                # 'pending' | 'ready' | 'completed'
    completed_at: Optional[datetime] = None


@dataclass
class MenuItem:
    id: str
    name: str
    category: str              # 'breakfast' | 'main' | 'all-day'
    price: float
    available: bool
    description: str = ""


# ════════════════════════════════════════════════════════
#  COLORS  (matches the green / orange prototype palette)
# ════════════════════════════════════════════════════════

C = {
    "green_dark":   "#1B5E20",
    "green_mid":    "#2E7D32",
    "orange":       "#FF9843",
    "orange_hover": "#e8882e",
    "white":        "#FFFFFF",
    "gray_bg":      "#F3F4F6",
    "gray_border":  "#E5E7EB",
    "gray_text":    "#6B7280",
    "gray_dark":    "#111827",
    "red":          "#DC2626",
    "blue":         "#2563EB",
    "yellow_bg":    "#FEF9C3",
    "yellow_border":"#FDE047",
    "app_bg":       "#F0F4F0",
}

PAYMENT_ICONS = {
    "credit":    "💳",
    "debit":     "💳",
    "cash":      "👛",
    "meal-plan": "🪪",
}

CATEGORY_COLORS = {
    "breakfast": ("#854D0E", "#FEF9C3"),
    "main":      ("#9A3412", "#FFEDD5"),
    "all-day":   ("#14532D", "#DCFCE7"),
}

CATEGORY_LABELS = {
    "breakfast": "Breakfast",
    "main":      "Main Menu",
    "all-day":   "All Day",
}


# ════════════════════════════════════════════════════════
#  STATE  (mirrors orderContext.tsx)
# ════════════════════════════════════════════════════════

class AppState:
    """
    Central state manager — equivalent to React Context.
    Replace the in-memory lists with Firebase calls in the
    DATABASE HOOK sections below.
    """

    def __init__(self):
        self._orders: list = []
        self._order_history: list = []
        self._next_order_number: int = 1
        self._current_date: date = date.today()
        self._total_completed: int = 0
        self._listeners: list = []
        self._next_id: int = 1
        self._menu_items: list = self._default_menu()

    def subscribe(self, callback: Callable):
        self._listeners.append(callback)

    def _notify(self):
        for cb in self._listeners:
            cb()

    def check_date_reset(self):
        today = date.today()
        if today != self._current_date:
            self._current_date = today
            self._next_order_number = 1
            self._total_completed = 0
            self._notify()

    def get_pending_orders(self) -> list:
        return [o for o in self._orders if o.status == "pending"]

    def get_history(self) -> list:
        return list(self._order_history)

    @property
    def total_completed(self) -> int:
        return self._total_completed

    @property
    def next_order_number(self) -> int:
        return self._next_order_number

    def add_order(self, user_id: str, user_name: str,
                  payment_type: str, items: list) -> Order:
        """
        ── DATABASE HOOK: ADD ORDER ──────────────────────────
        In production, orders are created by the Android app.
        Replace this method with a Firebase real-time listener
        that fires whenever a new order document is added.

        Example (Firestore):
            def on_new_order(col_snapshot, changes, read_time):
                for change in changes:
                    if change.type.name == 'ADDED':
                        data = change.document.to_dict()
                        items = [OrderItem(i['name'], i['quantity'],
                                           i.get('notes',''))
                                 for i in data.get('items', [])]
                        order = Order(
                            id=int(change.document.id, 16),
                            order_number=data['orderNumber'],
                            user_id=data['userId'],
                            user_name=data['userName'],
                            payment_type=data.get('paymentType','cash'),
                            items=items,
                            timestamp=data['createdAt'].datetime,
                            status=data['status'],
                        )
                        self._orders.append(order)
                        self._notify()   # <-- triggers UI refresh

            query = db.collection('orders').where('status','in',
                        ['pending','in-progress'])
            self._watcher = query.on_snapshot(on_new_order)
        ─────────────────────────────────────────────────────
        """
        order = Order(
            id=self._next_id,
            order_number=str(self._next_order_number).zfill(4),
            user_id=user_id,
            user_name=user_name,
            payment_type=payment_type,
            items=items,
            timestamp=datetime.now(),
            status="pending",
        )
        self._next_id += 1
        self._next_order_number += 1
        self._orders.append(order)
        self._notify()
        return order

    def mark_order_ready(self, order_id: int):
        """
        ── DATABASE HOOK: MARK ORDER READY ──────────────────
        After updating local state, write the new status back
        to Firebase so the student's app is notified instantly.

        Example (Firestore):
            db.collection('orders').document(str(order_id)).update({
                'status': 'ready',
                'completedAt': firestore.SERVER_TIMESTAMP,
            })

        Firebase Cloud Messaging or a Firestore listener on the
        student's app will pick up the status change and notify
        the student their food is ready for pickup.
        ─────────────────────────────────────────────────────
        """
        order = next((o for o in self._orders if o.id == order_id), None)
        if order:
            order.status = "completed"
            order.completed_at = datetime.now()
            self._orders.remove(order)
            self._order_history.insert(0, order)
            self._total_completed += 1
            self._notify()

    # ── MENU ITEMS ───────────────────────────────────────

    def get_menu_items(self) -> list:
        return list(self._menu_items)

    def add_menu_item(self, name: str, category: str,
                      price: float, available: bool, description: str) -> MenuItem:
        """
        ── DATABASE HOOK: ADD MENU ITEM ─────────────────────
        Example (Firestore):
            doc_ref = db.collection('menuItems').add({
                'name': name, 'category': category,
                'price': price, 'available': available,
                'description': description,
            })
        ─────────────────────────────────────────────────────
        """
        item = MenuItem(
            id=str(int(time.time() * 1000)),
            name=name, category=category,
            price=price, available=available, description=description,
        )
        self._menu_items.append(item)
        self._notify()
        return item

    def update_menu_item(self, item_id: str, **kwargs):
        """
        ── DATABASE HOOK: UPDATE MENU ITEM ──────────────────
        Example (Firestore):
            db.collection('menuItems').document(item_id).update(kwargs)
        ─────────────────────────────────────────────────────
        """
        item = next((m for m in self._menu_items if m.id == item_id), None)
        if item:
            for k, v in kwargs.items():
                setattr(item, k, v)
            self._notify()

    def delete_menu_item(self, item_id: str):
        """
        ── DATABASE HOOK: DELETE MENU ITEM ──────────────────
        Example (Firestore):
            db.collection('menuItems').document(item_id).delete()
        ─────────────────────────────────────────────────────
        """
        self._menu_items = [m for m in self._menu_items if m.id != item_id]
        self._notify()

    def _default_menu(self) -> list:
        """
        ── DATABASE HOOK: LOAD MENU ON STARTUP ──────────────
        Replace this with a Firestore read:

        Example:
            docs = db.collection('menuItems').stream()
            return [MenuItem(id=d.id, **d.to_dict()) for d in docs]
        ─────────────────────────────────────────────────────
        """
        raw = [
            ("1",  "Scrambled Eggs",           "breakfast", 4.99, True,  ""),
            ("2",  "Pancakes",                  "breakfast", 5.99, True,  ""),
            ("3",  "Breakfast Burrito",          "breakfast", 6.99, True,  ""),
            ("4",  "Grilled Chicken Sandwich",   "main",      8.99, True,  ""),
            ("5",  "Caesar Salad",               "main",      7.99, True,  ""),
            ("6",  "Veggie Burger",              "main",      8.49, True,  ""),
            ("7",  "Pizza Slice (Pepperoni)",    "main",      3.99, True,  ""),
            ("8",  "Pasta Carbonara",            "main",      9.99, True,  ""),
            ("9",  "Fish Tacos",                 "main",      9.49, True,  ""),
            ("10", "French Fries",               "all-day",   2.99, True,  ""),
            ("11", "Sweet Potato Fries",         "all-day",   3.49, True,  ""),
            ("12", "Smoothie Bowl",              "all-day",   6.99, True,  ""),
            ("13", "Coffee",                     "all-day",   2.49, True,  ""),
            ("14", "Soda",                       "all-day",   1.99, True,  ""),
        ]
        return [MenuItem(*r) for r in raw]


# ════════════════════════════════════════════════════════
#  MOCK ORDER SIMULATOR
# ════════════════════════════════════════════════════════

MOCK_QUEUE = [
    ("STU-2847", "Sarah Johnson",  "meal-plan",
     [OrderItem("Grilled Chicken Sandwich", 1), OrderItem("French Fries", 1), OrderItem("Lemonade", 1)]),
    ("STU-1923", "Marcus Chen",    "credit",
     [OrderItem("Caesar Salad", 1, "No croutons please"), OrderItem("Iced Coffee", 2)]),
    ("STU-5621", "Emma Rodriguez", "debit",
     [OrderItem("Veggie Burger", 1, "Extra pickles"), OrderItem("Sweet Potato Fries", 1)]),
    ("STU-3304", "James Wilson",   "cash",
     [OrderItem("Pizza Slice (Pepperoni)", 2), OrderItem("Soda", 1)]),
    ("STU-7892", "Olivia Martinez","meal-plan",
     [OrderItem("Pasta Carbonara", 1), OrderItem("Garlic Bread", 1)]),
    ("STU-4156", "Liam Thompson",  "credit",
     [OrderItem("Fish Tacos", 3, "Mild sauce only"), OrderItem("Chips & Salsa", 1)]),
]


class MockOrderSimulator:
    """
    ── DATABASE HOOK: REPLACE ENTIRE CLASS ──────────────
    This simulates incoming orders every 5 seconds.
    In production, DELETE this class and instead set up a
    Firestore real-time listener inside AppState.add_order().
    ─────────────────────────────────────────────────────
    """
    def __init__(self, state: AppState):
        self._state = state
        self._index = 0
        self._running = False

    def start(self):
        self._running = True
        threading.Thread(target=self._loop, daemon=True).start()

    def stop(self):
        self._running = False

    def _loop(self):
        while self._running and self._index < len(MOCK_QUEUE):
            time.sleep(5)
            if not self._running:
                break
            uid, name, pay, items = MOCK_QUEUE[self._index]
            self._state.add_order(uid, name, pay, items)
            self._index += 1


# ════════════════════════════════════════════════════════
#  ORDER TICKET CARD  (mirrors OrderTicket.tsx)
# ════════════════════════════════════════════════════════

class OrderTicketCard(ctk.CTkFrame):
    """
    Self-contained ticket card.
    Supports animated removal: fade out → collapse height → destroy.
    """
    ANIM_FPS   = 60          # frames per second
    FADE_MS    = 220         # fade-out duration  (ms)
    SHRINK_MS  = 180         # height collapse    (ms)

    def __init__(self, parent, order: Order, on_ready: Callable, **kwargs):
        super().__init__(parent, fg_color=C["white"], corner_radius=12,
                         border_width=2, border_color=C["green_dark"], **kwargs)
        self._order    = order
        self._on_ready = on_ready
        self._alpha    = 1.0        # logical opacity (0–1)
        self._removing = False
        self._build()

    def _build(self):
        order = self._order

        # ── Orange header ────────────────────────────────
        header = ctk.CTkFrame(self, fg_color=C["orange"], corner_radius=0, height=82)
        header.pack(fill="x")
        header.pack_propagate(False)

        top_row = ctk.CTkFrame(header, fg_color="transparent")
        top_row.pack(fill="x", padx=10, pady=(8, 2))

        ctk.CTkLabel(top_row, text=f"Order #{order.order_number}",
                     font=ctk.CTkFont(size=15, weight="bold"),
                     text_color=C["white"]).pack(side="left")
        ctk.CTkLabel(top_row, text=f"🕐 {order.timestamp.strftime('%I:%M %p')}",
                     font=ctk.CTkFont(size=11),
                     text_color=C["white"]).pack(side="right")

        pay_icon  = PAYMENT_ICONS.get(order.payment_type, "💳")
        pay_label = ("Meal Plan" if order.payment_type == "meal-plan"
                     else order.payment_type.capitalize())

        ctk.CTkLabel(header,
                     text=f"👤 {order.user_name}   {pay_icon} {pay_label}",
                     font=ctk.CTkFont(size=12),
                     text_color=C["white"]).pack(anchor="w", padx=10)
        ctk.CTkLabel(header, text=f"ID: {order.user_id}",
                     font=ctk.CTkFont(size=10),
                     text_color=C["white"]).pack(anchor="w", padx=10)

        # ── Items list ───────────────────────────────────
        items_frame = ctk.CTkFrame(self, fg_color=C["white"])
        items_frame.pack(fill="x", padx=10, pady=(8, 2))

        ctk.CTkLabel(items_frame, text="Items:",
                     font=ctk.CTkFont(size=12, weight="bold"),
                     text_color=C["green_dark"]).pack(anchor="w")

        for item in order.items:
            row = ctk.CTkFrame(items_frame, fg_color=C["white"],
                               border_width=1, border_color=C["gray_border"],
                               corner_radius=8)
            row.pack(fill="x", pady=2)

            inner = ctk.CTkFrame(row, fg_color="transparent")
            inner.pack(fill="x", padx=8, pady=4)

            ctk.CTkLabel(inner, text=str(item.quantity),
                         font=ctk.CTkFont(size=11, weight="bold"),
                         text_color=C["white"], fg_color=C["orange"],
                         corner_radius=10, width=22, height=22).pack(side="left")
            ctk.CTkLabel(inner, text=f"  {item.name}",
                         font=ctk.CTkFont(size=12, weight="bold"),
                         text_color=C["gray_dark"]).pack(side="left")

            if item.notes:
                note = ctk.CTkFrame(row, fg_color=C["yellow_bg"],
                                    border_width=1, border_color=C["yellow_border"],
                                    corner_radius=6)
                note.pack(fill="x", padx=8, pady=(0, 4))
                ctk.CTkLabel(note, text=f"📝 {item.notes}",
                             font=ctk.CTkFont(size=10, slant="italic"),
                             text_color="#713F12").pack(anchor="w", padx=6, pady=3)

        # ── Mark as Ready button ─────────────────────────
        self._ready_btn = ctk.CTkButton(
            self, text="✓  Mark as Ready",
            font=ctk.CTkFont(size=13, weight="bold"),
            fg_color=C["green_dark"], hover_color=C["green_mid"],
            corner_radius=8, height=38,
            command=self._request_remove,
        )
        self._ready_btn.pack(fill="x", padx=10, pady=(6, 10))

    # ── ANIMATED REMOVAL ─────────────────────────────────
    def _request_remove(self):
        """Called when cook clicks ready. Animates out, then notifies state."""
        if self._removing:
            return
        self._removing = True
        self._ready_btn.configure(state="disabled", text="✓  Done!")
        # Step 1: fade to green tint
        self.configure(fg_color="#E8F5E9", border_color=C["green_mid"])
        self.after(60, self._start_fade)

    def _start_fade(self):
        """Fade the card by blending border/bg toward background color."""
        steps      = max(1, int(self.FADE_MS / (1000 / self.ANIM_FPS)))
        interval   = int(self.FADE_MS / steps)
        self._fade_steps = steps
        self._fade_step  = 0
        self._fade_interval = interval
        self._fade_tick()

    def _fade_tick(self):
        if not self.winfo_exists():
            return
        self._fade_step += 1
        t = self._fade_step / self._fade_steps          # 0 → 1
        # Interpolate border color from green_mid → app_bg
        border = self._lerp_color(C["green_mid"], C["app_bg"], t)
        bg     = self._lerp_color("#E8F5E9",      C["app_bg"], t)
        self.configure(fg_color=bg, border_color=border)

        if self._fade_step < self._fade_steps:
            self.after(self._fade_interval, self._fade_tick)
        else:
            self._start_shrink()

    def _start_shrink(self):
        """Collapse the card height to 0, then fire the state update."""
        try:
            self.update_idletasks()
            full_h = self.winfo_height()
        except Exception:
            full_h = 200
        steps    = max(1, int(self.SHRINK_MS / (1000 / self.ANIM_FPS)))
        interval = int(self.SHRINK_MS / steps)
        self._shrink_full     = full_h
        self._shrink_steps    = steps
        self._shrink_step     = 0
        self._shrink_interval = interval
        self.configure(height=full_h)
        self.pack_propagate(False)
        self._shrink_tick()

    def _shrink_tick(self):
        if not self.winfo_exists():
            return
        self._shrink_step += 1
        t = self._shrink_step / self._shrink_steps
        ease = t * t * (3 - 2 * t)                     # smoothstep
        new_h = int(self._shrink_full * (1.0 - ease))
        self.configure(height=max(new_h, 0))

        if self._shrink_step < self._shrink_steps:
            self.after(self._shrink_interval, self._shrink_tick)
        else:
            # Animation done — notify state (triggers grid reflow)
            self._on_ready(self._order.id)

    @staticmethod
    def _lerp_color(hex_a: str, hex_b: str, t: float) -> str:
        """Linear interpolation between two hex colors."""
        def parse(h):
            h = h.lstrip("#")
            return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))
        ra, ga, ba = parse(hex_a)
        rb, gb, bb = parse(hex_b)
        r = int(ra + (rb - ra) * t)
        g = int(ga + (gb - ga) * t)
        b = int(ba + (bb - ba) * t)
        return f"#{r:02x}{g:02x}{b:02x}"


# ════════════════════════════════════════════════════════
#  KITCHEN DISPLAY TAB  (mirrors KitchenDisplay.tsx)
#  Uses a responsive grid (4 columns) instead of a single
#  column. Cards are added/removed surgically — no full
#  redraws. Removal triggers the card's own fade+shrink
#  animation; the grid reflows naturally afterward.
# ════════════════════════════════════════════════════════

class KitchenDisplayTab(ctk.CTkFrame):
    COLS = 4   # tickets per row — change to 3 if window is narrower

    def __init__(self, parent, state: AppState, toast_fn: Callable, **kwargs):
        super().__init__(parent, fg_color=C["app_bg"], **kwargs)
        self._state    = state
        self._toast    = toast_fn
        self._cards: dict = {}          # order_id → OrderTicketCard
        self._order_seq: list = []      # insertion-order list of order ids
        self._empty_lbl = None
        self._build()
        state.subscribe(self._on_state_change)

    def _build(self):
        # ── Stats bar ────────────────────────────────────
        stats = ctk.CTkFrame(self, fg_color=C["white"], corner_radius=0, height=72)
        stats.pack(fill="x")
        stats.pack_propagate(False)

        left = ctk.CTkFrame(stats, fg_color="transparent")
        left.pack(side="left", padx=20, pady=10)
        ctk.CTkLabel(left, text="🧑‍🍳  Kitchen Display",
                     font=ctk.CTkFont(size=16, weight="bold"),
                     text_color=C["green_dark"]).pack(anchor="w")
        ctk.CTkLabel(left, text="Live order queue",
                     font=ctk.CTkFont(size=11),
                     text_color=C["gray_text"]).pack(anchor="w")

        right = ctk.CTkFrame(stats, fg_color="transparent")
        right.pack(side="right", padx=20, pady=10)

        self._active_pill = self._stat_pill(right, "0", "Active Orders",   C["green_dark"])
        self._active_pill.pack(side="left", padx=6)
        self._done_pill   = self._stat_pill(right, "0", "Completed Today", C["orange"])
        self._done_pill.pack(side="left", padx=6)

        # ── Scrollable grid ──────────────────────────────
        self._scroll = ctk.CTkScrollableFrame(self, fg_color=C["app_bg"])
        self._scroll.pack(fill="both", expand=True, padx=16, pady=12)

        # Configure 4 equal-weight columns inside the scrollable frame
        for col in range(self.COLS):
            self._scroll.columnconfigure(col, weight=1, uniform="ticket_col")

        self._next_row = 0   # next grid row to place a card into
        self._next_col = 0   # next grid column

    def _stat_pill(self, parent, num, label, color):
        pill = ctk.CTkFrame(parent, fg_color=color, corner_radius=10)
        lbl  = ctk.CTkLabel(pill, text=num,
                            font=ctk.CTkFont(size=26, weight="bold"),
                            text_color=C["white"])
        lbl.pack(padx=16, pady=(6, 0))
        ctk.CTkLabel(pill, text=label, font=ctk.CTkFont(size=11),
                     text_color=C["white"]).pack(padx=16, pady=(0, 6))
        pill._num = lbl
        return pill

    # ── STATE OBSERVER ───────────────────────────────────
    def _on_state_change(self):
        """Called by AppState._notify(). Only adds new cards; removal
        is triggered by the card's own animation → _handle_ready()."""
        pending     = self._state.get_pending_orders()
        pending_ids = {o.id for o in pending}

        # Update counters only
        self._active_pill._num.configure(text=str(len(pending)))
        self._done_pill._num.configure(text=str(self._state.total_completed))

        # Add cards for brand-new orders
        for order in pending:
            if order.id not in self._cards:
                self._add_card(order)

        # Clean up any cards whose orders vanished without animation
        # (e.g. external cancellation from Firebase)
        for oid in list(self._cards.keys()):
            if oid not in pending_ids:
                card = self._cards.pop(oid)
                if oid in self._order_seq:
                    self._order_seq.remove(oid)
                if card.winfo_exists():
                    card.destroy()
                self._reflow_grid()

        self._toggle_empty_state(len(pending) == 0)

    def _add_card(self, order: Order):
        """Place a new card into the next available grid cell."""
        card = OrderTicketCard(
            self._scroll, order,
            on_ready=self._handle_ready,
        )
        card.grid(row=self._next_row, column=self._next_col,
                  padx=6, pady=6, sticky="nsew")

        self._cards[order.id]   = card
        self._order_seq.append(order.id)

        # Advance position
        self._next_col += 1
        if self._next_col >= self.COLS:
            self._next_col = 0
            self._next_row += 1

        # Slide-in: brief scale flash via border pulse
        self._pulse_new(card)

        self._toggle_empty_state(False)

    def _pulse_new(self, card: "OrderTicketCard", step: int = 0):
        """Quick border highlight on newly arrived card."""
        colors = [C["orange"], C["green_dark"], C["orange"], C["green_dark"]]
        if not card.winfo_exists() or step >= len(colors):
            return
        card.configure(border_color=colors[step])
        card.after(120, lambda: self._pulse_new(card, step + 1))

    def _handle_ready(self, order_id: int):
        """Called AFTER the card's animation completes."""
        order = next((o for o in self._state.get_pending_orders()
                      if o.id == order_id), None)

        # Remove card widget (animation already finished, widget is tiny)
        card = self._cards.pop(order_id, None)
        if order_id in self._order_seq:
            self._order_seq.remove(order_id)
        if card and card.winfo_exists():
            card.destroy()

        # Update state (this triggers _on_state_change → counter update only)
        if order:
            self._state.mark_order_ready(order_id)
            self._toast(f"✅  Order #{order.order_number} Ready!\n"
                        f"Notification sent to {order.user_name}")

        # Reflow remaining cards into clean grid positions
        self._reflow_grid()
        self._toggle_empty_state(len(self._cards) == 0)

    def _reflow_grid(self):
        """Re-grid all remaining cards in insertion order — no rebuild."""
        row, col = 0, 0
        for oid in self._order_seq:
            card = self._cards.get(oid)
            if card and card.winfo_exists():
                card.grid(row=row, column=col,
                          padx=6, pady=6, sticky="nsew")
                col += 1
                if col >= self.COLS:
                    col = 0
                    row += 1
        self._next_row = row
        self._next_col = col

    def _toggle_empty_state(self, show: bool):
        if show:
            if not self._empty_lbl:
                self._empty_lbl = ctk.CTkFrame(self._scroll, fg_color=C["white"],
                                               corner_radius=20)
                self._empty_lbl.grid(row=0, column=0,
                                     columnspan=self.COLS,
                                     padx=40, pady=60, sticky="nsew")
                ctk.CTkLabel(self._empty_lbl, text="🧑‍🍳",
                             font=ctk.CTkFont(size=52)).pack(pady=(30, 5))
                ctk.CTkLabel(self._empty_lbl, text="All caught up! 🎉",
                             font=ctk.CTkFont(size=22, weight="bold"),
                             text_color=C["green_dark"]).pack()
                ctk.CTkLabel(self._empty_lbl, text="Waiting for new orders...",
                             font=ctk.CTkFont(size=14),
                             text_color=C["gray_text"]).pack(pady=(4, 30))
        else:
            if self._empty_lbl:
                self._empty_lbl.destroy()
                self._empty_lbl = None


# ════════════════════════════════════════════════════════
#  ORDER HISTORY TAB  (mirrors OrderHistoryTab)
# ════════════════════════════════════════════════════════

class OrderHistoryTab(ctk.CTkFrame):
    def __init__(self, parent, state: AppState, **kwargs):
        super().__init__(parent, fg_color=C["app_bg"], **kwargs)
        self._state = state
        self._search_var = tk.StringVar()
        self._filter_var = tk.StringVar(value="all")
        self._date_var   = tk.StringVar(value=date.today().strftime("%Y-%m-%d"))
        self._build()
        state.subscribe(self._refresh_results)

    def _build(self):
        # Filter card
        fc = ctk.CTkFrame(self, fg_color=C["white"], corner_radius=14,
                          border_width=2, border_color=C["gray_border"])
        fc.pack(fill="x", padx=20, pady=(20, 10))

        ctk.CTkLabel(fc, text="Filters",
                     font=ctk.CTkFont(size=16, weight="bold"),
                     text_color=C["green_dark"]).pack(anchor="w", padx=16, pady=(12, 6))

        row = ctk.CTkFrame(fc, fg_color="transparent")
        row.pack(fill="x", padx=16, pady=(0, 14))

        # Date
        c1 = ctk.CTkFrame(row, fg_color="transparent")
        c1.pack(side="left", expand=True, fill="x", padx=(0, 8))
        ctk.CTkLabel(c1, text="📅 Date", font=ctk.CTkFont(size=12),
                     text_color=C["gray_text"]).pack(anchor="w")
        ctk.CTkEntry(c1, textvariable=self._date_var,
                     height=36, placeholder_text="YYYY-MM-DD").pack(fill="x", pady=2)

        # Filter type
        c2 = ctk.CTkFrame(row, fg_color="transparent")
        c2.pack(side="left", expand=True, fill="x", padx=8)
        ctk.CTkLabel(c2, text="Filter By", font=ctk.CTkFont(size=12),
                     text_color=C["gray_text"]).pack(anchor="w")
        ctk.CTkOptionMenu(c2, variable=self._filter_var,
                          values=["all", "order", "user"],
                          height=36, fg_color=C["white"],
                          button_color=C["orange"], text_color=C["gray_dark"],
                          command=lambda _: self._refresh_results()
                          ).pack(fill="x", pady=2)

        # Search
        c3 = ctk.CTkFrame(row, fg_color="transparent")
        c3.pack(side="left", expand=True, fill="x", padx=(8, 0))
        ctk.CTkLabel(c3, text="🔍 Search", font=ctk.CTkFont(size=12),
                     text_color=C["gray_text"]).pack(anchor="w")
        ctk.CTkEntry(c3, textvariable=self._search_var,
                     placeholder_text="Search orders...",
                     height=36).pack(fill="x", pady=2)

        self._search_var.trace_add("write", lambda *_: self._refresh_results())
        self._date_var.trace_add("write", lambda *_: self._refresh_results())

        # Results card
        rc = ctk.CTkFrame(self, fg_color=C["white"], corner_radius=14,
                          border_width=2, border_color=C["gray_border"])
        rc.pack(fill="both", expand=True, padx=20, pady=(0, 20))

        self._res_header = ctk.CTkLabel(rc, text="Results (0)",
                                        font=ctk.CTkFont(size=16, weight="bold"),
                                        text_color=C["green_dark"])
        self._res_header.pack(anchor="w", padx=16, pady=(12, 6))

        self._res_scroll = ctk.CTkScrollableFrame(rc, fg_color=C["white"])
        self._res_scroll.pack(fill="both", expand=True, padx=8, pady=(0, 8))

    def _refresh_results(self, *_):
        for w in self._res_scroll.winfo_children():
            w.destroy()

        q      = self._search_var.get().lower()
        ftype  = self._filter_var.get()
        seldat = self._date_var.get()
        filtered = []

        for o in self._state.get_history():
            if o.completed_at:
                if o.completed_at.strftime("%Y-%m-%d") != seldat:
                    continue
            if q:
                if ftype == "order":
                    if q not in o.order_number.lower(): continue
                elif ftype == "user":
                    if q not in o.user_id.lower() and q not in o.user_name.lower(): continue
                else:
                    if (q not in o.order_number.lower()
                            and q not in o.user_id.lower()
                            and q not in o.user_name.lower()):
                        continue
            filtered.append(o)

        self._res_header.configure(text=f"Results ({len(filtered)})")

        if not filtered:
            ctk.CTkLabel(self._res_scroll,
                         text="No orders found for the selected filters",
                         font=ctk.CTkFont(size=13),
                         text_color=C["gray_text"]).pack(pady=40)
            return

        for order in filtered:
            self._build_row(order)

    def _build_row(self, order: Order):
        card = ctk.CTkFrame(self._res_scroll, fg_color=C["white"],
                            border_width=2, border_color=C["gray_border"],
                            corner_radius=12)
        card.pack(fill="x", pady=4, padx=4)

        top = ctk.CTkFrame(card, fg_color="transparent")
        top.pack(fill="x", padx=10, pady=(8, 4))

        badge = ctk.CTkFrame(top, fg_color=C["orange"], corner_radius=8)
        badge.pack(side="left")
        ctk.CTkLabel(badge, text=f"#{order.order_number}",
                     font=ctk.CTkFont(size=14, weight="bold"),
                     text_color=C["white"]).pack(padx=10, pady=6)

        info = ctk.CTkFrame(top, fg_color="transparent")
        info.pack(side="left", padx=12)
        ctk.CTkLabel(info, text=f"👤 {order.user_name}",
                     font=ctk.CTkFont(size=13, weight="bold"),
                     text_color=C["gray_dark"]).pack(anchor="w")
        ctk.CTkLabel(info, text=f"ID: {order.user_id}",
                     font=ctk.CTkFont(size=11),
                     text_color=C["gray_text"]).pack(anchor="w")

        times = ctk.CTkFrame(top, fg_color="transparent")
        times.pack(side="right")
        ctk.CTkLabel(times, text=f"Ordered:   {order.timestamp.strftime('%H:%M:%S')}",
                     font=ctk.CTkFont(size=11), text_color=C["gray_text"]).pack(anchor="e")
        if order.completed_at:
            ctk.CTkLabel(times,
                         text=f"Completed: {order.completed_at.strftime('%H:%M:%S')}",
                         font=ctk.CTkFont(size=11), text_color=C["gray_text"]).pack(anchor="e")

        ib = ctk.CTkFrame(card, fg_color=C["gray_bg"], corner_radius=8)
        ib.pack(fill="x", padx=10, pady=(0, 8))
        ctk.CTkLabel(ib, text="Items:", font=ctk.CTkFont(size=11),
                     text_color=C["gray_text"]).pack(anchor="w", padx=8, pady=(4, 0))
        for item in order.items:
            line = f"  {item.quantity}×  {item.name}"
            if item.notes:
                line += f"  ({item.notes})"
            ctk.CTkLabel(ib, text=line, font=ctk.CTkFont(size=12),
                         text_color=C["gray_dark"]).pack(anchor="w", padx=8)
        ctk.CTkFrame(ib, height=6, fg_color="transparent").pack()


# ════════════════════════════════════════════════════════
#  MENU MANAGEMENT TAB  (mirrors MenuManagementTab)
# ════════════════════════════════════════════════════════

class MenuManagementTab(ctk.CTkFrame):
    def __init__(self, parent, state: AppState, **kwargs):
        super().__init__(parent, fg_color=C["app_bg"], **kwargs)
        self._state = state
        self._cat_filter  = tk.StringVar(value="all")
        self._editing_id: Optional[str] = None
        self._build()
        state.subscribe(self._refresh_grid)

    def _build(self):
        hdr = ctk.CTkFrame(self, fg_color="transparent")
        hdr.pack(fill="x", padx=20, pady=(20, 0))

        left = ctk.CTkFrame(hdr, fg_color="transparent")
        left.pack(side="left")
        ctk.CTkLabel(left, text="Menu Items",
                     font=ctk.CTkFont(size=22, weight="bold"),
                     text_color=C["green_dark"]).pack(anchor="w")
        ctk.CTkLabel(left, text="Manage dishes and availability",
                     font=ctk.CTkFont(size=12),
                     text_color=C["gray_text"]).pack(anchor="w")

        ctk.CTkButton(hdr, text="＋  Add Menu Item",
                      fg_color=C["green_dark"], hover_color=C["green_mid"],
                      font=ctk.CTkFont(size=13, weight="bold"),
                      height=40, corner_radius=10,
                      command=self._show_form).pack(side="right")

        # Form (hidden initially)
        self._form_outer = ctk.CTkFrame(self, fg_color=C["white"], corner_radius=14,
                                        border_width=2, border_color=C["orange"])

        # Category filter
        cat_row = ctk.CTkFrame(self, fg_color=C["white"], corner_radius=10,
                               border_width=2, border_color=C["gray_border"])
        cat_row.pack(fill="x", padx=20, pady=12)

        for cat in ("all", "breakfast", "main", "all-day"):
            lbl = "All Items" if cat == "all" else CATEGORY_LABELS[cat]
            ctk.CTkButton(cat_row, text=lbl,
                          fg_color=C["orange"] if cat == "all" else C["gray_bg"],
                          hover_color=C["orange_hover"],
                          text_color=C["white"] if cat == "all" else C["gray_dark"],
                          font=ctk.CTkFont(size=12, weight="bold"),
                          height=36, width=110, corner_radius=8,
                          command=lambda c=cat: self._set_cat(c),
                          ).pack(side="left", padx=6, pady=6)

        self._grid_scroll = ctk.CTkScrollableFrame(self, fg_color=C["app_bg"])
        self._grid_scroll.pack(fill="both", expand=True, padx=20, pady=(0, 16))

        self._refresh_grid()

    def _set_cat(self, cat: str):
        self._cat_filter.set(cat)
        self._refresh_grid()

    # ── FORM ─────────────────────────────────────────────
    def _show_form(self, item: Optional[MenuItem] = None):
        self._form_outer.pack(fill="x", padx=20, pady=(4, 0))
        for w in self._form_outer.winfo_children():
            w.destroy()

        self._editing_id = item.id if item else None
        title = "Edit Menu Item" if item else "Add New Menu Item"

        ctk.CTkLabel(self._form_outer, text=title,
                     font=ctk.CTkFont(size=16, weight="bold"),
                     text_color=C["green_dark"]).pack(anchor="w", padx=16, pady=(12, 8))

        row1 = ctk.CTkFrame(self._form_outer, fg_color="transparent")
        row1.pack(fill="x", padx=16)

        def col(parent, label_text):
            c = ctk.CTkFrame(parent, fg_color="transparent")
            c.pack(side="left", expand=True, fill="x", padx=4)
            ctk.CTkLabel(c, text=label_text, font=ctk.CTkFont(size=12),
                         text_color=C["gray_text"]).pack(anchor="w")
            return c

        c1 = col(row1, "Dish Name *")
        self._f_name = ctk.CTkEntry(c1, height=36,
                                    placeholder_text="e.g. Grilled Chicken Sandwich")
        self._f_name.pack(fill="x")
        if item: self._f_name.insert(0, item.name)

        c2 = col(row1, "Category *")
        self._f_cat = ctk.CTkOptionMenu(c2, values=["breakfast", "main", "all-day"],
                                        height=36, fg_color=C["white"],
                                        button_color=C["orange"],
                                        text_color=C["gray_dark"])
        self._f_cat.pack(fill="x")
        if item: self._f_cat.set(item.category)

        c3 = col(row1, "Price ($) *")
        self._f_price = ctk.CTkEntry(c3, height=36, placeholder_text="0.00")
        self._f_price.pack(fill="x")
        if item: self._f_price.insert(0, str(item.price))

        self._f_avail = tk.BooleanVar(value=item.available if item else True)
        avail_row = ctk.CTkFrame(self._form_outer, fg_color="transparent")
        avail_row.pack(fill="x", padx=16, pady=6)
        ctk.CTkCheckBox(avail_row, text="Available for ordering",
                        variable=self._f_avail,
                        fg_color=C["green_dark"],
                        hover_color=C["green_mid"]).pack(side="left")

        ctk.CTkLabel(self._form_outer, text="Description (optional)",
                     font=ctk.CTkFont(size=12),
                     text_color=C["gray_text"]).pack(anchor="w", padx=16)
        self._f_desc = ctk.CTkTextbox(self._form_outer, height=55)
        self._f_desc.pack(fill="x", padx=16, pady=(2, 8))
        if item and item.description:
            self._f_desc.insert("0.0", item.description)

        btn_row = ctk.CTkFrame(self._form_outer, fg_color="transparent")
        btn_row.pack(fill="x", padx=16, pady=(0, 14))

        label = "Update" if item else "Add"
        ctk.CTkButton(btn_row, text=f"✓  {label} Item",
                      fg_color=C["green_dark"], hover_color=C["green_mid"],
                      height=36, width=140, corner_radius=8,
                      command=self._submit).pack(side="left", padx=(0, 8))
        ctk.CTkButton(btn_row, text="✕  Cancel",
                      fg_color=C["gray_border"], hover_color="#D1D5DB",
                      text_color=C["gray_dark"],
                      height=36, width=120, corner_radius=8,
                      command=self._hide_form).pack(side="left")

    def _submit(self):
        name = self._f_name.get().strip()
        cat  = self._f_cat.get()
        avail = self._f_avail.get()
        desc = self._f_desc.get("0.0", "end").strip()

        if not name:
            messagebox.showwarning("Missing field", "Please enter a dish name.")
            return
        try:
            price = float(self._f_price.get().strip())
        except ValueError:
            messagebox.showwarning("Invalid price", "Please enter a valid number.")
            return

        if self._editing_id:
            self._state.update_menu_item(self._editing_id,
                                         name=name, category=cat, price=price,
                                         available=avail, description=desc)
        else:
            self._state.add_menu_item(name, cat, price, avail, desc)
        self._hide_form()

    def _hide_form(self):
        self._form_outer.pack_forget()
        self._editing_id = None

    # ── GRID ─────────────────────────────────────────────
    def _refresh_grid(self, *_):
        for w in self._grid_scroll.winfo_children():
            w.destroy()

        cat = self._cat_filter.get()
        items = [m for m in self._state.get_menu_items()
                 if cat == "all" or m.category == cat]

        COLS = 3
        row_frame = None
        for i, item in enumerate(items):
            if i % COLS == 0:
                row_frame = ctk.CTkFrame(self._grid_scroll, fg_color="transparent")
                row_frame.pack(fill="x", pady=4)
            self._build_card(row_frame, item)

    def _build_card(self, parent, item: MenuItem):
        card = ctk.CTkFrame(parent, fg_color=C["white"], corner_radius=12,
                            border_width=2, border_color=C["gray_border"])
        card.pack(side="left", expand=True, fill="both", padx=4)

        top = ctk.CTkFrame(card, fg_color="transparent")
        top.pack(fill="x", padx=10, pady=(10, 0))

        info = ctk.CTkFrame(top, fg_color="transparent")
        info.pack(side="left", fill="both", expand=True)
        ctk.CTkLabel(info, text=item.name,
                     font=ctk.CTkFont(size=13, weight="bold"),
                     text_color=C["gray_dark"]).pack(anchor="w")
        ctk.CTkLabel(info, text=f"${item.price:.2f}",
                     font=ctk.CTkFont(size=16, weight="bold"),
                     text_color=C["orange"]).pack(anchor="w")

        btns = ctk.CTkFrame(top, fg_color="transparent")
        btns.pack(side="right")
        ctk.CTkButton(btns, text="✏", width=32, height=32, corner_radius=8,
                      fg_color="#DBEAFE", hover_color="#BFDBFE",
                      text_color=C["blue"],
                      command=lambda i=item: self._show_form(i)).pack(pady=1)
        ctk.CTkButton(btns, text="🗑", width=32, height=32, corner_radius=8,
                      fg_color="#FEE2E2", hover_color="#FECACA",
                      text_color=C["red"],
                      command=lambda i=item: self._confirm_delete(i)).pack(pady=1)

        if item.description:
            ctk.CTkLabel(card, text=item.description,
                         font=ctk.CTkFont(size=11), text_color=C["gray_text"],
                         wraplength=180).pack(anchor="w", padx=10, pady=2)

        footer = ctk.CTkFrame(card, fg_color=C["gray_bg"], corner_radius=0, height=32)
        footer.pack(fill="x", pady=(6, 0))
        footer.pack_propagate(False)

        text_c, bg_c = CATEGORY_COLORS.get(item.category, (C["gray_text"], C["gray_bg"]))
        ctk.CTkLabel(footer, text=CATEGORY_LABELS.get(item.category, item.category),
                     font=ctk.CTkFont(size=10, weight="bold"),
                     text_color=text_c, fg_color=bg_c,
                     corner_radius=6).pack(side="left", padx=8, pady=4)

        dot = "● Available" if item.available else "● Unavailable"
        dot_color = C["green_dark"] if item.available else C["red"]
        ctk.CTkLabel(footer, text=dot, font=ctk.CTkFont(size=11),
                     text_color=dot_color).pack(side="right", padx=8)

    def _confirm_delete(self, item: MenuItem):
        if messagebox.askyesno("Delete item", f'Delete "{item.name}"?'):
            self._state.delete_menu_item(item.id)


# ════════════════════════════════════════════════════════
#  ADMIN PANEL  (mirrors AdminPanel.tsx — tabbed)
# ════════════════════════════════════════════════════════

class AdminPanelTab(ctk.CTkFrame):
    def __init__(self, parent, state: AppState, **kwargs):
        super().__init__(parent, fg_color=C["app_bg"], **kwargs)
        self._state = state
        self._build()

    def _build(self):
        tab_bar = ctk.CTkFrame(self, fg_color=C["white"], corner_radius=0, height=60)
        tab_bar.pack(fill="x")
        tab_bar.pack_propagate(False)

        inner = ctk.CTkFrame(tab_bar, fg_color="transparent")
        inner.pack(side="left", padx=20, pady=10)

        self._hist_btn = ctk.CTkButton(
            inner, text="📋  Order History",
            font=ctk.CTkFont(size=13, weight="bold"),
            fg_color=C["orange"], hover_color=C["orange_hover"],
            height=38, width=165, corner_radius=10,
            command=self._show_history)
        self._hist_btn.pack(side="left", padx=(0, 8))

        self._menu_btn = ctk.CTkButton(
            inner, text="🍽  Menu Management",
            font=ctk.CTkFont(size=13, weight="bold"),
            fg_color=C["gray_bg"], hover_color=C["gray_border"],
            text_color=C["gray_dark"],
            height=38, width=185, corner_radius=10,
            command=self._show_menu)
        self._menu_btn.pack(side="left")

        content = ctk.CTkFrame(self, fg_color="transparent")
        content.pack(fill="both", expand=True)

        self._history_tab = OrderHistoryTab(content, self._state)
        self._menu_tab    = MenuManagementTab(content, self._state)

        self._show_history()

    def _show_history(self):
        self._menu_tab.pack_forget()
        self._history_tab.pack(fill="both", expand=True)
        self._hist_btn.configure(fg_color=C["orange"], text_color=C["white"])
        self._menu_btn.configure(fg_color=C["gray_bg"], text_color=C["gray_dark"])

    def _show_menu(self):
        self._history_tab.pack_forget()
        self._menu_tab.pack(fill="both", expand=True)
        self._menu_btn.configure(fg_color=C["orange"], text_color=C["white"])
        self._hist_btn.configure(fg_color=C["gray_bg"], text_color=C["gray_dark"])


# ════════════════════════════════════════════════════════
#  TOAST NOTIFICATION
# ════════════════════════════════════════════════════════

class Toast(ctk.CTkToplevel):
    def __init__(self, parent, message: str):
        super().__init__(parent)
        self.overrideredirect(True)
        self.attributes("-topmost", True)
        self.configure(fg_color=C["green_dark"])
        ctk.CTkLabel(self, text=message, font=ctk.CTkFont(size=13),
                     text_color=C["white"], wraplength=300).pack(padx=20, pady=12)
        px = parent.winfo_x() + parent.winfo_width()  - 360
        py = parent.winfo_y() + parent.winfo_height() - 120
        self.geometry(f"+{px}+{py}")
        self.after(3000, self.destroy)


# ════════════════════════════════════════════════════════
#  ROOT WINDOW  (mirrors Root.tsx)
# ════════════════════════════════════════════════════════

class CafeteriaApp(ctk.CTk):
    def __init__(self):
        super().__init__()
        ctk.set_appearance_mode("light")
        ctk.set_default_color_theme("green")

        self.title("🍽️  Cafeteria Order Management")
        self.geometry("1280x800")
        self.minsize(900, 600)
        self.configure(fg_color=C["app_bg"])

        self._state = AppState()
        self._simulator = MockOrderSimulator(self._state)
        self._build()

        # ── DATABASE HOOK: REMOVE THIS LINE WHEN CONNECTING TO FIREBASE ──
        # Remove self._simulator.start() and set up your Firestore listener instead.
        self._simulator.start()

        self.after(60_000, self._date_check_loop)

    def _date_check_loop(self):
        self._state.check_date_reset()
        self.after(60_000, self._date_check_loop)

    def _build(self):
        # Navbar
        navbar = ctk.CTkFrame(self, fg_color=C["green_dark"], height=68, corner_radius=0)
        navbar.pack(fill="x")
        navbar.pack_propagate(False)

        ctk.CTkLabel(navbar, text="🍽️  Cafeteria Order Management",
                     font=ctk.CTkFont(size=20, weight="bold"),
                     text_color=C["white"]).pack(side="left", padx=20)

        self._clock = ctk.CTkLabel(navbar, text="",
                                   font=ctk.CTkFont(size=13),
                                   text_color="#FFFFFF")
        self._clock.pack(side="right", padx=20)
        self._tick_clock()

        nav_btns = ctk.CTkFrame(navbar, fg_color="transparent")
        nav_btns.pack(side="right", padx=10)

        self._nav_k = ctk.CTkButton(
            nav_btns, text="🧑‍🍳  Kitchen Display",
            fg_color=C["orange"], hover_color=C["orange_hover"],
            font=ctk.CTkFont(size=13, weight="bold"),
            height=40, width=175, corner_radius=8,
            command=self._show_kitchen)
        self._nav_k.pack(side="left", padx=4)

        self._nav_a = ctk.CTkButton(
            nav_btns, text="⚙️  Admin Panel",
            fg_color="transparent", hover_color=C["green_mid"],
            border_color=C["white"], border_width=1,
            font=ctk.CTkFont(size=13, weight="bold"),
            text_color=C["white"],
            height=40, width=150, corner_radius=8,
            command=self._show_admin)
        self._nav_a.pack(side="left", padx=4)

        # Content
        content = ctk.CTkFrame(self, fg_color="transparent")
        content.pack(fill="both", expand=True)

        self._kitchen_tab = KitchenDisplayTab(content, self._state, self._toast)
        self._admin_tab   = AdminPanelTab(content, self._state)

        self._show_kitchen()

    def _show_kitchen(self):
        self._admin_tab.pack_forget()
        self._kitchen_tab.pack(fill="both", expand=True)
        self._nav_k.configure(fg_color=C["orange"])
        self._nav_a.configure(fg_color="transparent")

    def _show_admin(self):
        self._kitchen_tab.pack_forget()
        self._admin_tab.pack(fill="both", expand=True)
        self._nav_a.configure(fg_color=C["green_mid"])
        self._nav_k.configure(fg_color="transparent")

    def _toast(self, message: str):
        Toast(self, message)

    def _tick_clock(self):
        self._clock.configure(text=datetime.now().strftime("%A  %H:%M:%S"))
        self.after(1000, self._tick_clock)

    def on_close(self):
        self._simulator.stop()
        self.destroy()


# ════════════════════════════════════════════════════════
#  ENTRY POINT
# ════════════════════════════════════════════════════════

if __name__ == "__main__":
    app = CafeteriaApp()
    app.protocol("WM_DELETE_WINDOW", app.on_close)
    app.mainloop()
