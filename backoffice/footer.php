        </div> <!-- End of Scrollable View -->
    </main> <!-- End of Content Grid -->
    
    <script>
        // Automatic fading out for alerts/feedbacks
        document.addEventListener("DOMContentLoaded", function() {
            const alerts = document.querySelectorAll('.auto-dismiss');
            alerts.forEach(function(alert) {
                setTimeout(function() {
                    alert.style.transition = 'opacity 0.5s ease';
                    alert.style.opacity = '0';
                    setTimeout(() => alert.remove(), 500);
                }, 4000);
            });
        });
    </script>
</body>
</html>
